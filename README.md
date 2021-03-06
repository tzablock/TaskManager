**Requirements:**  
Maximum supported processes:   
processes.max - amount of maximum supported processes, add(process) won't work if there are more processes  
methods:  
• Add a process  
add(process)  
• List running processes  
list()  
sorting them by time of creation  
sorting them by SID  
sorting them by priority  
• Kill/KillGroup/KillAll  
kill(SID)  
kill([SID1, SID2])  
killAll()  
options on start of the app:  
1. Accept only process when amount of processes is less than processes.max.  
2. When processes.max reached removed the oldest one.  
3. When processes.max reached remove the oldest with the lowest priority.  

**Solution:**  
Description:  
One task represents one process and contain all information needed to crete process.  
Initialization of Task mean correspond process will be created.  
Success in creation of task mean as well success in creation and triggering run of process.  
Finished tasks, what mean finished processes are removed on call to REST api /listRunningTasks or /addTask.  
(For heavy workload I would implement some cyclical check like those built in into Spring if tasks are finished, but as it add complexity and delay on request for this exercise is minor, there is no sense for it)  
  

Task (is immutable):  
fields:  
  SID - immutable, autogenerated unique  
  priority - immutable, values: low, medium, high  
  type - immutable, values: macos, linux, windows (we can give value "test" to make mocked Process running forever)  
  creationTime- immutable, autogenerated time of Task creation  
  command - immutable, command to be run (we can give value "command" to make mocked Process running forever)  
  process - mutable, assigned process to this task  
methods:  
  kill() - destroy assigned process  


Dependencies:  
Lombok added to avoid boilerplate code like getters and setters.  
jackson-datatype-jsr310 added to support LocalDataTime by jackson.  
For unit testing mockito-core, assertj-core.  

Potential feature:  
* give choice to sort both asc and desc  
* serialization of tasks(some DB, NoSQL, HDFS ...)  
* security (Spring security etc.)  
* extend amount of available processes  

Tests:  
A lot of tests of spring components are made as tests of POJO. The reason is simple, such tests are just quicker  
as we don't initialize in them spring, as are the same good as we don't need to test framework as I believe that  
authors of Spring did it better than me.  
There is one exception for test of Controller as I wanted to do Integration Test for easy present whole functionality.  

**Testing rest api locally:**  
1. Got to resources/application.properties and set properties:  
processes.max - maximal amount of tasks/processes.(Default is 5)  
processes.management.strategy - according to requirements options are:  
        only_less_than_max - there need to be maximal <processes.max> amount of tasks/processes  
        remove_oldest - when we already <processes.max> amount of tasks/processes we remove the oldest task/process and add new one  
        remove_lowest_priority - when we already <processes.max> amount of tasks/processes we remove the oldest with the lowest priority task/process and add new one  
2. Run Spring starter class: com.swissre.RestServiceApplication.  
3. For debug/review purpose for IntelliJ it is recommended to install Lombok plugin.  
3. Usage of REST api:  
a) To add task  
type: POST  
ulr: http://localhost:8080/addTask  
body:  
{  
          "priority": "low, medium, high",  
          "type": "bash, windows, macos",  
          "command": "<your_command>"  
}  
example  
{  
          "priority": "low",  
          "type": "test",  
          "command": "command"  
}  
response:  
{"sid": "4b2f01e5-417c-4b14-af68-989500022e2f", "priority": "LOW", "creationTime": 2022-02-20T12:31:03.204458, "type": "TEST"}  

**IMPORTANT!**  
To test in easy way my functionality I've added command "command" or type "test" which will make Task live forever! (fake process)  
for this there are small changes to be in normal case removed like:  
1) src.main.java.com.swissre.taskmanagement.ProcessRunner  
line 16  
condition task.getType() != TaskType.TEST ? returning optional null to be removed  
2) src.main.java.com.swissre.taskmanagement.entity.Task  
line 67-69 and 74-76 to be removed.  
I did it as probably you have a lot of apps to check and I don't want to waste your time  
and at the same time I like to make something working.  


b) To list active tasks:  
type: GET  
ulr: http://localhost:8080/listRunningTasks?sort=<sid,creationtime,priority or just empty>  
response:  
[  
  {"sid": "25c539b1-e046-401f-a173-31c9b1e81f35", "priority": "LOW", "creationTime": 2022-02-20T12:30:15.758692, "type": "BASH"},  
  ...  
]  
parameters:  
sort - sorting by sid is ascending, by creation time is ascending and by priority is descending  

c) To kill task/tasks/all tasks  
type: POST  
url: http://localhost:8080/killTasks?sids=<sid/sids of tasks to be killed or empty to kill all tasks>  
body: empty  
response:  
[<sids of killed tasks>]  
parameters:  
sids - 1)one sid for one task to kill, 2)multiple sids in format sid1,sid2.. to kill multiple tasks and 3)no sids or empty sids parameter to kill all tasks  

Or just run Integration test with example:  
src/test/java/com/swissre/controller/TasksControllerIT.java  
Just remove @Disabled annotations which are added as those scenarios are covered by unit tests.  

Explanation:  
src/main/java/com/swissre/controller/TasksController  
Is simple Spring controller for REST api described above.  
src/main/java/com/swissre/taskmanagement/entity/InitTask and Task   
Where InitTask is object got from REST request from user based on which immutable Task is created which describe information needed to create Process as well as Process.  
src/main/java/com/swissre/taskmanagement/exception/*  
I created exceptions with meaningful names as I've managed task creation problems with use of them.  
I chose so as for this problem functional solution is not needed and standard exception management gives  
easy and clean way to deal with unusual scenarios.  
src/main/java/com/swissre/taskmanagement/options/*  
I've created enums for predefined values to force use in code only accepted values and prevent all sort of typos etc.  
src/main/java/com/swissre/taskmanagement/verification/*  
I've created in abstract class TaskQueueManager generic code to manage list of tasks, their adding  
and removal etc. and specific classes per strategy, chosen by setting property processes.management.strategy with strategy specific code.  
src/main/java/com/swissre/taskmanagement/ProcessRunner  
Is a class which manage running Processes per specific system. I've created it to add some real functionality for this exercise.  
(threat management is not optimal but I'm not an expert from threats as I worked mostly with distributed systems  
that is why I've used mostly parallel processing on VMs scale not explicitly on their threats)  
src/main/java/com/swissre/taskmanagement/TaskManager.java  
It's the service that provide functionality to manage operations on Tasks.  
src/main/java/com/swissre/RestServiceApplication.java   
Standard Spring boot runner class.  
src/main/resources/application.properties  
Properties for our application.  

Of course to this there is Unit Test coverage as well as one Integration Test to present functionality:  
src/test/java/com/swissre/controller/TasksControllerIT  