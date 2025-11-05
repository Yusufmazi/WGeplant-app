#!/bin/bash

# add your IP-Address of the server on the BW-Cloud.
TEST_SERVER_IP="exampleServerIP"

# add your server-protocol (e.g., https) and server-port (e.g., 443)
PROTOCOL="exampleServerProtocol"
SERVER_PORT="exampleServerPort"

TEST_SERVER_URL="$PROTOCOL://$TEST_SERVER_IP:$SERVER_PORT"

# package containing the e2e tests that should get executed
TEST_PACKAGE="com.wgeplant.e2eTests"

echo "Start the E2E-Test for the package: $TEST_PACKAGE"

# check if the server is reachable
echo "Wait till the server under: $TEST_SERVER_URL is reachable..."
for i in {1..20}; do
  HTTP_CODE=$(curl -s -k -o /dev/null -w "%{http_code}" "$TEST_SERVER_URL")

  if [[ "$HTTP_CODE" =~ ^(200|404)$ ]]; then
    # Server is ready, even if it returns a 404 (Not Found)
    echo "Server is ready. HTTP-Statuscode: $HTTP_CODE"
    break
  fi

  echo "Attempt $i of 20: Server isn't ready yet. Wait 2 seconds... HTTP-Statuscode: $HTTP_CODE"
  sleep 2
done

# if the server doesn't answer after 20 attempts
if [[ ! "$HTTP_CODE" =~ ^(200|404)$ ]]; then
  echo "Error: The server is not reachable or couldn't be started."
  exit 1
fi

# execute the e2e tests
echo ""
echo "Start the android-e2e-tests..."
./gradlew :app:connectedUiTestingDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.package="$TEST_PACKAGE"

# check exit code of the test
if [ $? -eq 0 ]; then
  echo ""
  echo "E2E-Tests successfully finished."
else
  echo ""
  echo "E2E-Tests failed."
fi

read -n 1 -s
