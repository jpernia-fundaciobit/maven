cmd /C mvn clean install -DskipTests
cmd /C mvn exec:java -Dexec.mainClass="org.fundaciobit.githubmanager.GitHubManager" -Dexec.classpathScope="test"
