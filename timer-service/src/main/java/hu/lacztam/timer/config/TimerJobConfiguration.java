package hu.lacztam.timer.config;

import com.github.kagkarlsson.scheduler.task.CompletionHandler;
import com.github.kagkarlsson.scheduler.task.ExecutionContext;
import com.github.kagkarlsson.scheduler.task.TaskInstance;
import com.github.kagkarlsson.scheduler.task.TaskWithDataDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.CustomTask;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import hu.lacztam.timer.model.Context;
import hu.lacztam.timer.model.UserEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

import javax.annotation.PostConstruct;

import static hu.lacztam.timer.config.JmsConfig.IN_MEMORY_KEEPASS_TIMER_HAS_EXPIRED;

@Configuration
public class TimerJobConfiguration {

    @Autowired JmsTemplate jmsTemplate;

    private static ConfigProperties config;
    @Autowired
    ConfigProperties configProperties;

    @PostConstruct
    public void init(){
        this.config = configProperties;
    }

    public static final String schedulerName = "timer-scheduler_";

    public static final TaskWithDataDescriptor<UserEmail> TIMER_TASK
            = new TaskWithDataDescriptor<>("timer-task", UserEmail.class);

    public static void start(Context ctx) {
        UserEmail userEmail = new UserEmail(ctx.getEmail());

        String schedulerInstance = schedulerName + ctx.getEmail();

        ctx.getSchedulerClient().schedule(
                                    TIMER_TASK.instance(schedulerInstance, userEmail),
                                    config.getIdle().getIdleTimer()
        );
//        Instant.now().plusSeconds(2)
//        config.getIdle().getIdleTimer()
    }

    @Bean
    public CustomTask<UserEmail> idleTimerListenerTask() {
        return Tasks.custom(TIMER_TASK)
                .execute((TaskInstance<UserEmail> taskInstance, ExecutionContext executionContext) -> {

                    String email = null;
                    if(taskInstance.getData() != null){
                        email = taskInstance.getData().getEmail();
                    }

                    //because at application start the taskInstance is null
                    if(email == null)
                        return new CompletionHandler.OnCompleteRemove<>();

                    jmsTemplate.convertAndSend(IN_MEMORY_KEEPASS_TIMER_HAS_EXPIRED, email);
                    return new CompletionHandler.OnCompleteRemove<>();
                });
    }
}