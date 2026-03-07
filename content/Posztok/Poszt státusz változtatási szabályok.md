---
title: "Poszt státusz változtatási szabályok"
---
[[Poszt státuszok|Lehetséges poszt státuszok]]

```java
if (isClosed(post))
{throw new IllegalStateException("A poszt már le van zárva");}
// ...
switch (currentStatus) {

    case "accepted" -> {
        if (!isEmployee || !"started".equals(targetStatusName)) {
            throw new IllegalStateException("Az alkalmazott csak 'started' státuszra válthat.");
        }
    }

    case "started" -> {
        if (!isEmployee || !"completed_by_employee".equals(targetStatusName)) {
            throw new IllegalStateException("Az alkalmazott csak 'completed_by_employee' státuszra válthat.");
        }
    }

    case "completed_by_employee" -> {
        if (!isEmployer ||
                (!"work_rejected".equals(targetStatusName) && !"closed".equals(targetStatusName))) {
            throw new IllegalStateException("A munkáltató csak 'work_rejected' vagy 'closed' státuszt választhat.");
        }
    }

    case "work_rejected" -> {
        if (!isEmployer ||
                (!"started".equals(targetStatusName) && !"unsuccessful_result_closed".equals(targetStatusName))) {
            throw new IllegalStateException("A munkáltató csak 'started' vagy 'unsuccessful_result_closed' státuszt választhat.");
        }
    }
}
```