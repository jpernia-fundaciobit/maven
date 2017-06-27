package org.fundaciobit.githubmanager;

/**
 * 
 * @author anadal
 *
 */
public class GitHubUser implements Comparable<GitHubUser> {

  String nom;

  String email;

  String username;

  /**
   * 
   */
  public GitHubUser() {
    super();
  }

  /**
   * @param username
   * @param nom
   * @param email
   */
  public GitHubUser(String username, String nom, String email) {
    super();
    this.username = username;
    this.nom = nom;
    this.email = email;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public int compareTo(GitHubUser o) {
    
    return this.username.compareToIgnoreCase(o.username);
  }
  
  @Override
  public String toString() {
    return username + "\t" + nom + "\t" + email;
  }
  

}
