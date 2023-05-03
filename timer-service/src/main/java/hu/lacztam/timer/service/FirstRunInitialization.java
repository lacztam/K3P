package hu.lacztam.timer.service;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@AllArgsConstructor
public class FirstRunInitialization {

    private final JdbcTemplate jdbcTemplate;

    public boolean checkIfTableExists(){
        String checkTable = "select 1 from scheduled_tasks";

        try{
            jdbcTemplate.execute(checkTable);
            return true;
        }catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }
    }

    public boolean createTableForScheduledTasks(){

        String createTable = "create table scheduled_tasks (\n" +
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

        String createIndex1 = "CREATE INDEX execution_time_idx ON scheduled_tasks (execution_time);";
        String createIndex2 = "CREATE INDEX last_heartbeat_idx ON scheduled_tasks (last_heartbeat);";

        jdbcTemplate.execute(createTable);
        jdbcTemplate.execute(createIndex1);
        jdbcTemplate.execute(createIndex2);

        return true;
    }

}
