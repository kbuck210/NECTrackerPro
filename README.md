# NECTrackerPro
Masters Project Repo  

Setup Instructions:  
Install Glassfish 4  
Install MySQL Server, client (and workbench if desired)  
Modify domainX domain.xml file to add JDBC Resource & connection pool (admin console broken...)  
Add MySQL Driver .jar file to domain /lib directory  
Start the domain & launch admin console  
Deploy .war file  

Instal Instructions:  
1. Pull from Master  
2. Correct Glassfish 4 server Runtime if necessary (master name - Glassfish4)  
3. Edit persistence.xml to match MySQL login connection  
4. Create local directory for NECTrackerResources outside of git root  
5. Modify glassfish-web.xml to point altdocroots to 4.)  
