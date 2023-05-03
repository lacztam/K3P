package hu.lacztam.timer.model;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.transaction.support.TransactionTemplate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Context {
    private SchedulerClient schedulerClient;
    private TransactionTemplate tx;
    private String email;

    public Context(SchedulerClient schedulerClient, TransactionTemplate tx) {
        this.schedulerClient = schedulerClient;
        this.tx = tx;
    }

}