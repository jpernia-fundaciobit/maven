package org.fundaciobit.githubmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;


import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;

public class GitHubUtils {
  
  
  

  
  public static GitHubClient getGitHubClient(String login_username, String login_password) throws IOException,
  FileNotFoundException {
    GitHubClient client = new GitHubClient();
    

    System.out.println(" Connectant com '" + login_username + "'");
    
    client.setCredentials(login_username, login_password);
    
    /*
    String repositori = configGH.getProperty("repositori");
    RepositoryId repository = getRepository(configGH, repositori);
    return new GitHubConfig(client, repository);
    */
    
    return client;
}

  public static Properties readPropertiesFile(String githubpropfile) throws IOException,
      FileNotFoundException {
    Properties configGH = new Properties();
    
    configGH.load(new FileInputStream(githubpropfile));
    return configGH;
  }

  public static RepositoryId getGitHubRepository(Properties configGH, String repositori) {
    String entitat = configGH.getProperty("entitat");
    RepositoryId repository = RepositoryId.create(entitat, repositori);
    return repository;
  }
  
  public static RepositoryId getGitHubRepository(String organitzacio, String repositori) {
    
    RepositoryId repository = RepositoryId.create(organitzacio, repositori);
    return repository;
  }
  
  
  
  
}
