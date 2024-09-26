- [ ] Basic task #task #status/today
- [ ] This Week task #task #status/thisweek
- [ ] Task with subtasks #task #status/today 
	- [ ] Subtask 1
	- [ ] Subtask 2

# Codeblock

```minion
query: tasks
display: list
heading: Foo
groupBy: parent_tag
groupByField: status
groupByOrder:
  - /today AS Today
  - /thisweek AS This Week
```
