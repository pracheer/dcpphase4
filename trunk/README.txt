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
	
server.txt contains ipaddress of each server and each GUI, as well as the oracle.
config.txt contains the name of all servers that will get automatically registered with Oracle when the oracle starts. So the user does not have to manually input all servers when the system starts. Also, the oracle will display the updated view of the chain as a new server gets added to it.

4. Click the LAUNCH.CMD file. Clicking on LAUNCH.CMD will start 6 servers (2 services each with 3 servers) and 2 corresponding GUIs. They will use the topology specified in TOPOLOGY_FILE.TXT (specifying bidirectional links) and SERVERS.TXT.

5. The Oracle GUI will also start by Launch.cmd

6. The oracle start command additionally takes another argument as a config file. This config file is described above.

7. When any server starts, it takes the name of the group that it belongs to as a command line argument. 

8. Naming Convention:
	a. groups/services are named as "01", "02" and so on
	b. Gui is named as G01 if it belongs to group 01
	c. server name is of format Saa_bb. Here aa is the group name that server belongs to and bb 	indicates the server number whithin the group. So, S01_01 is server which belongs to group 01
	d. The ordering of servers is called a View and ordering is not based on server names. View 	is used to implement ordedring. First element in the view is the Head of the chain, last 	element of view the Tail of the chain.
	e. Oracle is called "Oracle" and assigned a group "00". By giving it a name and assigning it 	a group, we are able to include it in the topology_file.txt and servers.txt

9. Oracle GUI, contains a display such that each updation of view is shown as it happens. After view gets updated, messages are sent to interested parties.


10. To use the Server GUI:
Account numbers are entered in the following format bb.xxxxx where bb is the branch_id used to start the server.
By default a new serial number is generated along with last used details.
The user can edit the serial number text field with the old serial number to re-execute a transaction.
Note: if an old serial number is used, details in other fields are ignored.


Authors of Phase 1:
Nikhil Kejriwal nk395
Pracheer Gupta pg298
Quazi Hussain qsh2