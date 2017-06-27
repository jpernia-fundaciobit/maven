package org.fundaciobit.githubmanager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Key;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.Team;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.TeamService;
import org.eclipse.egit.github.core.service.UserService;


public class GitHubTest {

  public static void main(String[] args) {
    try {
      

      Properties configGH = GitHubUtils.readPropertiesFile("gh.properties");
      
      
      String login_username = configGH.getProperty("username");
      String login_password = configGH.getProperty("password");
      String organitzacio = configGH.getProperty("entitat");

      GitHubClient client = GitHubUtils.getGitHubClient(login_username, login_password);
      
      System.out.println(" Organitzacio = " + organitzacio);

      
      
      LabelService labelService = new LabelService(client);
      IssueService issueService = new IssueService(client);
      
      // RepositoryId repository = GitHubManager.selectRepos(client, organitzacio, login_username);
      
     String project = "genapp-1.0";
      
      RepositoryId repository =  GitHubUtils.getGitHubRepository(organitzacio,project);
      
      
      List<Label> labels = labelService.getLabels(repository);
      
      // 0.- Substiruir "help wanted" per "helpwanted"
     
      for (Label label : labels) {
       
       if (label.getName().equals("help wanted")) {
         
         throw new Exception("Ha de reemplaçar l'etiqueta 'helpwanted' per 'helpwanted' al projecte " + repository.getName());
         
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
          } 
        }

      }
      
      
      
      
      
      
/*
      RepositoryService repositoryService = new RepositoryService(client);
      List<Repository> repos = repositoryService.getRepositories();
      
      
      Map<String, String> users = new TreeMap<String, String>();
      
      
      
      for (Repository repo : repos) {
     
            
      System.out.println(" ------------------  REPOS " + repo.getName()
          + "(" + repo.getLanguage() + ")\t" + repo.getOpenIssues() + " ----------------");
      System.out.println();
      
      
      
      
      RepositoryId repository = GitHubUtils.getGitHubRepository(configGH, repo.getName());
      
          
//          MilestoneService milestoneService = new MilestoneService(client);
//          
//          
//         
//          
//          List<Milestone> allMilestonesGH = milestoneService.getMilestones(repository, null);
//          for (Milestone milestone : allMilestonesGH) {
//            System.out.println("LIST Milestone GH[" + milestone.getNumber() + "]["
//                + milestone.getTitle() + "]");
//           // milestonesGH.put(milestone.getTitle(), milestone);
//          }
//          
//          
//          System.out.println();
//          System.out.println();
//          
//          LabelService labelService = new LabelService(client);
//          
//          List<Label> labels =  labelService.getLabels(repository);
//          
//          for (Label label : labels) {
//            System.out.println(label.getName() + "\t" + label.getColor() + "\t" + label.getUrl());
//          }
//          
//          System.out.println();
//          System.out.println();
          
          
          TeamService teamService = new TeamService(client);
          
          List<Team> teams;
          
          try {
          teams = teamService.getTeams(repository);
          } catch(Exception e) {
            continue;
          }
          
          for (Team team : teams) {
            //System.out.println(team.getId() + " " + team.getName() + "(" + team.getMembersCount() + ")"
            //   + "\t" + team.getPermission() + "\t" + team.getReposCount() + "\t" + team.getUrl());
            
            List<User> members = teamService.getMembers(team.getId());
            for (User user : members) {
              //System.out.println( "          - " +  user.getLogin() + "[" + user.getName()+ "] -> " + user.getEmail());
              //System.out.println(user.getType());
              
              users.put(user.getLogin(), user.getEmail() + "|" + user.getName());
            }
            
            
          }
        } // Final de for de repos
        
        
      
//        OrganizationService org = new OrganizationService(client);
//        
//        List<User> members = org.getPublicMembers(configGH.getProperty("entitat"));
//        
//        int count = 1;
//        for (User user : members) {
//          System.out.println(count + "          - " +  user.getLogin() + "[" + user.getName()
//              + "] -> " + user.getEmail());
//          count++;
//        }
      
      
      Properties usuaris = new Properties();
      usuaris.putAll(users);
      usuaris.store(new FileOutputStream("githubusers_updated.properties"), "githubusers");
      */
      
      
          
          
          
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }

  public static void replaceLabel(LabelService labelService, IssueService issueService,
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


}
