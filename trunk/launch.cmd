start java -cp "bank.jar" branch.server.BranchMachine -topology "topology_file.txt" -servers "servers.txt" -config "config.txt" -id M01
start java -cp "bank.jar" branch.server.BranchMachine -topology "topology_file.txt" -servers "servers.txt" -config "config.txt" -id M02

start java -cp "bank.jar" branch.server.BranchGUI -topology "topology_file.txt" -servers "servers.txt" -config "config.txt" -id G02
start java -cp "bank.jar" branch.server.BranchGUI -topology "topology_file.txt" -servers "servers.txt" -config "config.txt" -id G01