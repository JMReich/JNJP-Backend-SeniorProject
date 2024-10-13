package com.cloudstorage.secure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;

@SpringBootApplication
public class SecureCloudStorageApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecureCloudStorageApplication.class, args);
	}

	// This is a bean that creates a RestTemplate object that is used to make REST API calls
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) throws Exception {
		TrustManager[] trustAllCerts = new TrustManager[]{
				new X509TrustManager() {
					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(X509Certificate[] certs, String authType) {
					}
				}
		};

		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

		HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

		HostnameVerifier allHostsValid = (hostname, session) -> true;
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		ClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		return builder
				.requestFactory(() -> requestFactory)
				//.basicAuthentication("root", "root") // Temporary username and password for tetsting
				// Can be replacxed with: http://localhost:8080/ping?username=root&password=root
				.build();

	}
}
