package org.fundaciobit.githubmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.TeamService;

/**
 * 
 * @author anadal
 *
 */
public class GitHubManager {

  public static void main(String[] args) {
    try {

      Properties configGH = GitHubUtils.readPropertiesFile("gh.properties");
      String login_username = configGH.getProperty("username");
      String login_password = configGH.getProperty("password");
      String organitzacio = configGH.getProperty("entitat");

      GitHubClient client = GitHubUtils.getGitHubClient(login_username, login_password);

      System.out.println(" Organitzacio = " + organitzacio);

//      if (client != null) {
//          return;
//      }

      while (true) {
        int selection;
        Scanner input = new Scanner(System.in);
        System.out.println();
        System.out.println();
        System.out.println("Escull");
        System.out.println("-------------------------");
        System.out.println("1 - Imprimir usuaris locals");
        System.out.println("2 - Sincronitzar usuaris (nous i eliminats)");
        System.out.println("5 - Mails de tothom");
        System.out.println("6 - Mails de un repositori");
        System.out.println("7 - Llistar repositoris");
        System.out.println("8 - Normalitzar Labels");
        System.out.println("9 - Branques per repositori");
        
        System.out.println("0 - Quit");
        
        System.out.println();

        //
        // TODO
        // imprimirMailsRepositori(client, organitzacio, login_username);
//        normalitzarLabelsIssues();
//        if (client != null)
//          return;

        selection = input.nextInt();
        switch (selection) {
        case 1:
          imprimirUsuarisLocals(client);
          break;

        case 2:
          // TODO
          break;

        case 5:
          imprimirTotsMails();
          break;
          
        case 6:
          imprimirMailsRepositori(client, organitzacio, login_username);
          break;
          
        case 7:
          imprimirRepositoris(client, organitzacio, login_username);
          break;
          
        case 8:
          adaptarLabels(client, login_username, organitzacio);
          break;

        case 9:
          branquesPerProjecte(client, organitzacio, login_username);
          break;
          
        case 0:
          System.exit(0);

        default:
          System.err.println("Aquesta opcio no existeix.");
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  
  
  
  public static void imprimirRepositoris(GitHubClient client, String organitzacio,
      String login_user) throws Exception {
   
    List<Repository> repos = getRepositoris(client, login_user, organitzacio);

    for (Repository repository : repos) {
     
      System.out.println("+ " + repository.getName());
    }
  }
  
  
  public static void branquesPerProjecte(GitHubClient client, String organitzacio,
      String login_user) throws Exception {
    RepositoryService repositoryService = new RepositoryService(client);

    List<Repository> repos = getRepositoris(client, login_user, organitzacio);

    for (Repository repository : repos) {

      List<RepositoryBranch> list = repositoryService.getBranches(repository);
      System.out.print("+ " + repository.getName() + ":");
      for (RepositoryBranch branch : list) {
          System.out.print(" " + branch.getName());
      }
      System.out.println();

    }

  }
  
  
  

  public static void imprimirUsuarisLocals(GitHubClient client) throws Exception {

    List<GitHubUser> usuaris = new ArrayList<GitHubUser>(getUsuarisLocals().values());

    Collections.sort(usuaris);

    for (GitHubUser gitHubUser : usuaris) {
      System.out.println(gitHubUser.toString());
    }

    System.out.println(" Total: " + usuaris.size());

  }

  public static void imprimirTotsMails() throws Exception {

    List<GitHubUser> usuaris = new ArrayList<GitHubUser>(getUsuarisLocals().values());

    Collections.sort(usuaris);

    StringBuffer str = crearMailString(usuaris);

    System.out.println(str.toString());

  }

  protected static StringBuffer crearMailString(List<GitHubUser> usuaris) throws Exception {
    StringBuffer str = new StringBuffer();

    boolean buit = true;

    Properties whiteblacklist = GitHubUtils
        .readPropertiesFile("mails_white_black_list.properties");

    for (GitHubUser gitHubUser : usuaris) {
      String username = gitHubUser.getUsername();
      String value = whiteblacklist.getProperty(username);
      if ("false".equals(value)) {
        // System.out.println(" XYZ BlackList => " + username);
        continue;
      }

      if (buit == false) {
        str.append(',');
      }
      str.append(gitHubUser.getNom()).append('<').append(gitHubUser.getEmail()).append('>');

      buit = false;
    }

    if (buit) {
      return str;
    }

    // Afegir WHITE LIST
    Set<Object> keys = whiteblacklist.keySet();
    for (Object object : keys) {
      String key = (String) object;
      String value = whiteblacklist.getProperty(key);
      if (!"false".equals(value)) {
        if (buit == false) {
          str.append(',');
        }
        str.append(value).append('<').append(key).append('>');

      }
    }

    // TODO AFEGIR WHITE

    return str;
  }

  public static void imprimirMailsRepositori(GitHubClient client, String organitzacio, String login_user)
      throws Exception {

    RepositoryId repository = selectRepos(client, organitzacio, login_user);
    Set<String> usernames = getUsersOfRepository(client, repository);

    Map<String, GitHubUser> usuarisMap = getUsuarisLocals();

    List<GitHubUser> usuaris = new ArrayList<GitHubUser>();

    for (String username : usernames) {

      GitHubUser ghu = usuarisMap.get(username);
      if (ghu == null) {
        System.err.println(" L'usuari " + username
            + " no existeix en la BBDD local. Recomanam que sincronitzi.");
      } else {
        usuaris.add(ghu);
      }
    }

    Collections.sort(usuaris);
    StringBuffer str = crearMailString(usuaris);

    System.out.println(str.toString());

  }




  public static RepositoryId selectRepos(GitHubClient client, String organitzacio, String login_user)
      throws Exception {
    List<Repository> repos = getRepositoris(client, login_user, organitzacio);

    Map<Integer, String> reposMap = new TreeMap<Integer, String>();
    System.out.println();
    System.out.println(" Seleccioni un repositori:");
    int count = 1;
    for (Repository repo : repos) {
      System.out.println(count + ".-" + repo.getName() );
      reposMap.put(count, repo.getName());
      count++;
    }

    Scanner input = new Scanner(System.in);

    int r = input.nextInt();

    String reposName = reposMap.get(r);

    System.out.println(" reposName de la pos " + r + " es " + reposName);
    return GitHubUtils.getGitHubRepository(organitzacio, reposName);
  }

  public static Set<String> getUsersOfRepository(GitHubClient client, RepositoryId repository)
      throws Exception {

    TeamService teamService = new TeamService(client);

    List<Team> teams;
    Set<String> users = new HashSet<String>();

    try {
      teams = teamService.getTeams(repository);
    } catch (Exception e) {
      return users;
    }

    for (Team team : teams) {

      List<User> members = teamService.getMembers(team.getId());
      for (User user : members) {
        users.add(user.getLogin());
      }

    }
    return users;
  }

  public static List<Repository> getRepositoris(GitHubClient client, String loginUser, String organization) throws Exception {

    RepositoryService repositoryService = new RepositoryService(client);
    
    try {
      return repositoryService.getOrgRepositories(organization);
    } catch(Exception e) {
      return repositoryService.getRepositories(loginUser);
    }

  }

  public static Map<String, GitHubUser> getUsuarisLocals() throws Exception {
    Properties prop = GitHubUtils.readPropertiesFile("githubusers.properties");

    Set<Object> keys = prop.keySet();
    Map<String, GitHubUser> usuaris = new HashMap<String, GitHubUser>();
    for (Object key : keys) {
      String username = (String) key;

      String value = prop.getProperty(username);
      int pos = value.lastIndexOf('|');
      String nom = value.substring(0, pos + 1).replace("|", "").trim();
      String email = value.substring(pos + 1);

      usuaris.put(username, new GitHubUser(username, nom, email));

    }

    return usuaris;

  }
  
  
  
  public static void adaptarLabels(GitHubClient client, String loginUser, String organitzacio) throws Exception {

    LabelService labelService = new LabelService(client);
    IssueService issueService = new IssueService(client);
    
    RepositoryId repository = GitHubManager.selectRepos(client, organitzacio, loginUser);
    
   //String project = "genapp-1.0";
    
   // RepositoryId repository =  GitHubUtils.getGitHubRepository(organization,project);
    
    
    List<Label> labels = labelService.getLabels(repository);
    
    // 0.- Substiruir "help wanted" per "helpwanted"
   
    for (Label label : labels) {
     
     if (label.getName().equals("help wanted")) {
       
       throw new Exception("Ha de reemplaçar l'etiqueta 'help wanted' per 'helpwanted' al projecte " + repository.getName());
       
     }
    }
    
    

    // 1.- Convertir
    
    
    final String[][] labelsToReplace = {{"bug", "Tipus:Error", "fc2929"},
        {"enhancement", "Tipus:Millora", "84b6eb"},
        {"helpwanted", "Tipus:Consulta", "159818"},
        {"question", "Tipus:Consulta", "cc317c"},
        {"duplicate", "Resolucio:Duplicada", "cccccc"},
        {"invalid", "Resolucio:Invalida", "e6e6e6"},
        {"wontfix", "Resolucio:NoSolucionada", "ffffff"}};
    
    Set<String> labelNames = new HashSet<String>();
    
    for (Label label : labels) {
      System.out.println(label.getName() +  "\t" + label.getUrl() );
      
      
      
      // Replace Labels
      for (int i = 0; i < labelsToReplace.length; i++) {
        final String labelFrom = labelsToReplace[i][0];
        
        if (labelFrom.equals(label.getName())) {
          
          final String labelTo = labelsToReplace[i][1];
          final String labelToColor = labelsToReplace[i][2];
          replaceLabel(labelService, issueService, repository, labelFrom, labelTo,
              labelToColor);
          
          labelNames.add(labelTo);
          
        } else {
          labelNames.add(label.getName());
        }
        
      }

    }
    
    
    // 2.- Afegir Noves Etiquetes
    final String[][] newlabels =  { 
        { "Tipus:Analisi_Disseny", "fef2c0"},
        {"Tipus:BBDD", "bfd4f2"},
        {"Tipus:Compilacio_Construccio", "d4c5f9"},
        {"Tipus:Configuracio", "bfd4f2"},
        {"Tipus:Consulta", "c5def5"},
        {"Tipus:Documentacio", "bfdadc"},
        {"Tipus:Error", "e99695"},
        {"Tipus:Nova_Funcionalitat", "c2e0c6"},
        {"Tipus:Refactoritzacio", "006b75"},
        {"Tipus:Millora", "f9d0c4"},
        {"Versio:unplanned", "000000"},
        {"Versio:x.y.z", "000000"},
        {"Prioritat:Immediata", "ff0000"},
        {"Prioritat:Molt_Alta", "fc2929"},
        {"Prioritat:Alta", "d93f0b"},
        {"Prioritat:Normal", "fbca04"},
        {"Prioritat:Baixa", "0e8a16"},
        {"Prioritat:Molt_Baixa", "1d76db"},
        {"Lloc:General", "fef2c0"},
        {"Lloc:Core", "fc2929"},
        {"Lloc:EJB", "d93f0b"},
        {"Lloc:Test", "fbca04"},
        {"Lloc:Web", "0e8a16"},
        {"Lloc:WebServices", "1d76db"},
        {"Lloc:Plugin", "c5def5"},
        {"Resolucio:Duplicada", "cccccc"},
        {"Resolucio:Invalida", "e6e6e6"},
        {"Resolucio:NoSolucionada", "ffffff"}
    };
    
    for (int i = 0; i < newlabels.length; i++) {
      if (!labelNames.contains(newlabels[i][0])) {
        Label label = new Label();
        label.setColor(newlabels[i][1]);
        label.setName(newlabels[i][0]);

        labelService.createLabel(repository, label);
        System.out.println(" Creada Label " + newlabels[i][0]);
      }
    }
    
  }
  
  
  

  protected static void replaceLabel(LabelService labelService, IssueService issueService,
      RepositoryId repository, final String labelFrom, final String labelTo,
      final String labelToColor) throws IOException {
    
    
    System.out.println(" ---- replace Label " + labelFrom + " for " + labelTo);
    
    // Existeix la Label de destí ?
    Label tipus_error_label = null;
    try {
      tipus_error_label = labelService.getLabel(repository, labelTo);
    } catch(RequestException rq) {
    }
    
    if (tipus_error_label == null) {
      tipus_error_label = new Label();
      tipus_error_label.setColor(labelToColor);
      tipus_error_label.setName(labelTo);

      tipus_error_label = labelService.createLabel(repository, tipus_error_label);
    }

    // Cercar Issues amb aquest Label
    Map<String, String> filter = new HashMap<String, String>();
    filter.put(IssueService.FILTER_LABELS, labelFrom);
    List<Issue> issues = issueService.getIssues(repository, filter);

    System.out.println("ISSUE SIZE [" + labelFrom + "] = " + issues.size());
    for (Issue issue : issues) {
      System.out.println(issue.getTitle());
      List<Label> labelsIssue = issue.getLabels();
      labelsIssue.add(tipus_error_label);
      issue.setLabels(labelsIssue);
      
      //issueService.editIssue(repository, issue);
      
      labelService.setLabels(repository, String.valueOf(issue.getNumber()), labelsIssue);
      
    }
    
     // Eliminar Label Antiga
    labelService.deleteLabel(repository, labelFrom);
  }

  
  
  
  public static final void importerTiquets(GitHubClient client, String loginUser, String organitzacio) throws Exception {

    LabelService labelService = new LabelService(client);
    IssueService issueService = new IssueService(client);
    
    //RepositoryId repository = GitHubManager.selectRepos(client, organitzacio, loginUser);
    RepositoryId repository = GitHubUtils.getGitHubRepository(organitzacio, "portafib");
    
    
    try {
      
      Map<String, Label> githubLabels = new HashMap<String, Label>(); 
      
      List<Label> labels = labelService.getLabels(repository);
      
      // 0.- Substiruir "help wanted" per "helpwanted"
     
      for (Label label : labels) {
       
        githubLabels.put(label.getName(), label);
        
        System.out.println(label.getName());
        
      }
      
      
      
      Map<String, String> tracLabels = new HashMap<String, String>(); 
      
      tracLabels.put("Error", "Tipus:Error");
      tracLabels.put("Anàlisis/Disseny", "Tipus:Analisi_Disseny");
      tracLabels.put("Refactorització", "Tipus:Refactoritzacio");
      tracLabels.put("Generador", "Lloc:Core");
      tracLabels.put("Test/Proves", "Lloc:Test");
      tracLabels.put("Applet", "Lloc:Plugin");
      tracLabels.put("Base de dades", "Tipus:BBDD");
      tracLabels.put("Compilació/Construcció", "Tipus:Compilacio_Construccio");
      tracLabels.put("Nova funcionalitat", "Tipus:Nova_Funcionalitat");
      tracLabels.put("Millora", "Tipus:Millora");

    
    // Ticket    Resum   Tipus   Created   Descripció 
    
   
    String all = FileUtils.readFileToString(new File("PortaFIB_TRAC.csv"));
    
    BufferedReader buffer=new BufferedReader(new StringReader(all));
    String line;
    int count = 1;
    while( (line=buffer.readLine()) != null) {
      
       if (count == 1) {
         count++;
         continue;
       }
      
    
       // Check parts
       String[] parts = line.split("\t");
       
       if (parts.length != 5) {
         System.out.println("Error en linia [" + count + "] => Items = " +parts.length );
         System.err.println(line);
       }
    
       String tipus = parts[2].trim();
       
       
       if (!tracLabels.containsKey(tipus)) {
         System.err.println(tipus);
       }

       
       // 0 - item 
       // 1 - title
       // 2 - tipus
       // 3-  creat
       // 4.- descripcio
       
       
    String item = parts[0].replace("#", "");
    String title = parts[1];
    
    String creat = parts[3];
    String descripcioBase = parts[4];
    
    String url = "http://trac.ibit.org/portafib/ticket/" + item;

    // NOTA text descripció reemplaçar | per \n
    String descripcio = "Trac Item: " + item + "\n"
                       + "Trac URL: " + url + "\n"
                       + "Trac Creat: " + creat + "\n"
                       + descripcioBase;
   
    
    
  
    Issue issue = new Issue();
  
    
    issue.setTitle(title);
    issue.setBody(descripcio);
  
  
    issue.setBody(descripcio);
  
    //issue.setMilestone(milestone);
  
  
    // open, closed, or all
    issue.setState("open");
    
    
    List<Label> labelsIssue = new ArrayList<Label>();
    
    labelsIssue.add(githubLabels.get(tracLabels.get(tipus)));
    
    issue.setLabels(labelsIssue);
    
    issue = issueService.createIssue(repository, issue);
    

    System.out.println("{" + count + "} Creat issue amb ID=" + issue.getId() + "(Trac " + item + "). Title: " + title);
  
    Thread.sleep(5000);
       count ++;
    }
    
    
    System.out.println(" OK");

    } catch (Exception e) {
      // TODO: handle exception
      e.printStackTrace();
    }
}
  
  
  
  
  

}
