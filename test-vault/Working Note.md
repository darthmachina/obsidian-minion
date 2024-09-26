
- [ ] #task Task with subtasks #status/now 
	- [x] Sub 1
	- [ ] Sub 2


```minion
query: tasks
display: list
include:
  tags:
    - status/now
```
