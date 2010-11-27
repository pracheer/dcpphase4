start java -cp "bank.jar" branch.server.BranchServer -topology "topology_file.txt" -servers "servers.txt" -id S01_01 -group "01" 
start java -cp "bank.jar" branch.server.BranchServer -topology "topology_file.txt" -servers "servers.txt" -id S01_02 -group "01" 
start java -cp "bank.jar" branch.server.BranchServer -topology "topology_file.txt" -servers "servers.txt" -id S01_03 -group "01" 

start java -cp "bank.jar" branch.server.BranchServer -topology "topology_file.txt" -servers "servers.txt" -id S02_01 -group "02" 
start java -cp "bank.jar" branch.server.BranchServer -topology "topology_file.txt" -servers "servers.txt" -id S02_02 -group "02"
start java -cp "bank.jar" branch.server.BranchServer -topology "topology_file.txt" -servers "servers.txt" -id S02_03 -group "02"


start java -cp "bank.jar" branch.server.BranchGUI -topology "topology_file.txt" -servers "servers.txt" -id G02 -group "02" 
start java -cp "bank.jar" branch.server.BranchGUI -topology "topology_file.txt" -servers "servers.txt" -id G01 -group "01" 


start java -cp "bank.jar" branch.server.Oracle -topology "topology_file.txt" -servers "servers.txt" -id Oracle -group "00" -config "config.txt"