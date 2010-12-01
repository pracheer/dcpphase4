How to Install:

1. Unzip the src.zip

2. The src folder contains all the source files

3. src folders also contains another folder called Executable and Scripts. 
	This folder contains the following files:
	LAUNCH.CMD
	TOPOLOGY_FILE.TXT
	SERVERS.TXT
	CONFIG.TXT
	JAR (BANK.JAR)
	
server.txt contains ip-address of each server and each GUI
config.txt contains the initial configuration of the branch services as well as the FD service.

4. Click the LAUNCH.CMD file. Clicking on LAUNCH.CMD will start % JVM's (2 services each with 3 servers) and 2 corresponding GUIs. They will use the topology specified in TOPOLOGY_FILE.TXT (specifying bidirectional links) and SERVERS.TXT.


5. Naming Convention:
	a. groups/services are named as "01", "02" and so on
	b. Gui is named as G01 if it belongs to Group/Service:01
	c. server name is of format aa_Mbb. Here aa is the group name that server belongs to and Mbb indicates the JVM(processor) that it belogns to.
		So, 01_M01 is server which belongs to branch service 01 and runs in machine (JVM) M01 
	d. The ordering of servers is called a View and ordering is not based on server names.
		View is used to implement ordering. First element in the view is the Head of the chain, last element of view the Tail of the chain. This view is only internally maintained in the servers.


6. To use the Server GUI:
Account numbers are entered in the following format bb.xxxxx where bb is the branch_id used to start the server.
By default a new serial number is generated along with last used details.
The user can edit the serial number text field with the old serial number to re-execute a transaction.
Note: if an old serial number is used, details in other fields are ignored.


Authors of Phase 3:
Nikhil Kejriwal nk395
Pracheer Gupta pg298
Quazi Hussain qsh2