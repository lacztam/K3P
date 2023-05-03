package hu.lacztam.timer.jms;

import com.github.kagkarlsson.scheduler.SchedulerClient;
import hu.lacztam.timer.config.ConfigProperties;
import hu.lacztam.timer.config.TimerJobConfiguration;
import hu.lacztam.timer.model.Context;
import lombok.AllArgsConstructor;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

import static hu.lacztam.timer.config.JmsConfig.RESTART_IN_MEMORY_TIMER;
import static hu.lacztam.timer.config.JmsConfig.START_IN_MEMORY_KEEPASS_TIMER;

@Component
@AllArgsConstructor
public class InMemoryKeePassTimerConsumer {

    private final DataSource dataSource;
    private final TimerJobConfiguration timerJobConfiguration;
    private final ConfigProperties configProperties;

    @JmsListener(destination = START_IN_MEMORY_KEEPASS_TIMER)
    public void onKeePassTimerTaskConsumer(String email){
        TransactionTemplate tx = new TransactionTemplate();
        Context context = new Context(schedulerForTimerJob(), tx, email);

        timerJobConfiguration.start(context);
    }

    @JmsListener(destination = RESTART_IN_MEMORY_TIMER)
    public void onRestartTimerTaskConsumer(String email){
        String instanceName = timerJobConfiguration.schedulerName + email;

        schedulerForTimerJob()
                .reschedule(timerJobConfiguration.idleTimerListenerTask().instance(instanceName),
                            configProperties.getIdle().getIdleTimer()
                );
    }

    private SchedulerClient schedulerForTimerJob(){
        return SchedulerClient.Builder
                .create(dataSource, timerJobConfiguration.idleTimerListenerTask())
                .build();
    }
}