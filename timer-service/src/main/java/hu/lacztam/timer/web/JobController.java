package hu.lacztam.timer.web;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@AllArgsConstructor
public class JobController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/db/create")
    public boolean createTableForScheduledTasks(){

        String query1 = "create table scheduled_tasks (\n" +
                "  task_name text not null,\n" +
                "  task_instance text not null,\n" +
                "  task_data bytea,\n" +
                "  execution_time timestamp with time zone not null,\n" +
                "  picked BOOLEAN not null,\n" +
                "  picked_by text,\n" +
                "  last_success timestamp with time zone,\n" +
                "  last_failure timestamp with time zone,\n" +
                "  consecutive_failures INT,\n" +
                "  last_heartbeat timestamp with time zone,\n" +
                "  version BIGINT not null,\n" +
                "  PRIMARY KEY (task_name, task_instance) );";

        String query2 = "CREATE INDEX execution_time_idx ON scheduled_tasks (execution_time);";
        String query3 = "CREATE INDEX last_heartbeat_idx ON scheduled_tasks (last_heartbeat);";

        jdbcTemplate.execute(query1);
        jdbcTemplate.execute(query2);
        jdbcTemplate.execute(query3);

        return true;
    }
}
