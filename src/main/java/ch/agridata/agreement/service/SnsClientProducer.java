package ch.agridata.agreement.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * Producer for the AWS SNS client.
 * Manages the lifecycle and instantiation of the SnsClient bean for CDI injection.
 */

@ApplicationScoped
public class SnsClientProducer {
  @ConfigProperty(name = "quarkus.aws.region")
  String awsRegion;

  @Produces
  @ApplicationScoped
  public SnsClient snsClient() {
    DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder()
        .build();

    return SnsClient.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(credentialsProvider)
        .build();
  }

  public void close(@Disposes SnsClient client) {
    client.close();
  }
}
