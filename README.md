# Customer Migration Tool 
This tool is used to migrate IntSights customers into the Rapid7 platform. It takes as its input a CSV file with the following columns: 
#### 0: Account ID
#### 1: Customer Name
#### 2: Organization Name
#### 3: Organization Region
#### 4: Product Code
#### 5: User Email
#### 6: User First Name
#### 7: User Last Name
#### 8: User Platform Admin Status
#### 9: User Product Role(s)

## How to Run
- Navigate to ~/intsights-migration-tool
- run the startup script to bring up the GUI. ./startup.sh

## GUI Parameters
- API Host URL: API URL to use for the user migration. (e.g. for staging use 'https://api.ipims-int-1.us-east-1.ipims-staging.r7ops.com/api/')
- Public API Key: Public key to use for API authentication against Class 1 API. Here is a JIRA example of how to obtain this key https://issues.corp.rapid7.com/browse/PLAT-16107
- RBAC Service Key: Public key to use for API authentication against the RBAC API. Here is a JIRA example of how to obtain this key https://issues.corp.rapid7.com/browse/PLAT-15268
- R7 Consumer: HTTP Header required to match the Public API Key.

## State Files
- This tool creates 3 files on the first startup to maintain a record of the entities created. 
It's important to keep these files in sync to avoid creating duplicate records in the system. Lines can be removed from these files if accounts wish to be recreated. 
- **customers.csv:** Record of Customers created. Map of Customer Names to Customer IDs. 
- **customerOrganizations.csv:** Record of Organizations created for each customer. Map of Organization Names to Organization IDs. 
- **orgProducts.csv:** Record of OrgProducts created. Map of Organization IDs to Product Tokens. 


## How to Build
Run the following command to build this project

**mvn clean -DskipTests=true install**