package com.wgeplant.model.datasource

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This class is responsible to observe the network connection.
 */
class NetworkDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : NetworkDataSource {

    companion object {
        private const val AVAILABLE = true
        private const val UNAVAILABLE = false
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * This method checks whether the device has network connection or not and returns it with a flow.
     */
    override fun observeNetworkConnection(): Flow<Boolean> {
        return callbackFlow {
            val networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    launch { send(AVAILABLE) }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    launch { send(UNAVAILABLE) }
                }

                // react to network capabilities changes
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val isInternetAvailable =
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                    if (isInternetAvailable) {
                        launch { send(AVAILABLE) }
                    } else {
                        launch { send(UNAVAILABLE) }
                    }
                }
            }
            // check initial status and send
            val currentNetwork = connectivityManager.activeNetwork
            if (currentNetwork == null) {
                launch { send(UNAVAILABLE) }
            } else {
                val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
                if (capabilities != null &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ) {
                    launch { send(AVAILABLE) }
                } else {
                    launch { send(UNAVAILABLE) }
                }
            }

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

            awaitClose {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        }.distinctUntilChanged() // only emit when the network status changes
    }
}
