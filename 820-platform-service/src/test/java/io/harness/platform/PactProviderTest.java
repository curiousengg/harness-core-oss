package io.harness.platform;

import au.com.dius.pact.provider.junit.PactRunner;
import au.com.dius.pact.provider.junit.target.HttpTarget;
import au.com.dius.pact.provider.junitsupport.Consumer;
import au.com.dius.pact.provider.junitsupport.Provider;

import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.target.Target;
import au.com.dius.pact.provider.junitsupport.target.TestTarget;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;


@RunWith(PactRunner.class) // Say JUnit to run tests with custom Runner
@Provider("Gateway") // Set up name of tested provider
@Consumer("Admin-Portal")
@PactBroker(host = "localhost", port = "9292")
public class PactProviderTest {

    @BeforeClass
    public static void enablePublishingPact() {
        System.setProperty("pact.verifier.publishResults", "true");
    }

    @TestTarget
    public final Target target = new HttpTarget("https", "localhost", 9090);

    @State("version is 1.1")
    public void validVersion() {
        System.out.println(target);
    }

    @State("invalid property")
    public void invalidProperty() {
    }


}