package hu.lacztam.timer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;

@Component
@ConfigurationProperties(prefix = "hu.lacztam")
public class ConfigProperties {

    private Idle idle = new Idle();

    public Idle getIdle() {
        return idle;
    }

    public void setIdle(Idle idle) {
        this.idle = idle;
    }

    public static class Idle{
        private long timerInSecond;
        private ZoneId zoneId;
        private String zoneIdSpecification;
        private Instant idleTimer;

        public Instant getIdleTimer() {
            // TO-DO: 2 hours behind from current time
            return Instant.now()
                    .atZone(getZoneId())
                    .toInstant()
                    .plusSeconds(timerInSecond);

            // not working
//            return LocalDateTime.now()
//                    .toInstant(ZoneOffset.UTC)
//                    .plusSeconds(timerInSecond);

            // not working
//            return LocalDateTime.now(TimeZone.getTimeZone(this.zoneIdSpecification).toZoneId())
//                                        .toInstant(ZoneOffset.UTC).plusSeconds(timerInSecond);
        }

        public void setIdleTimer(Instant idleTimer) {
            this.idleTimer = idleTimer;
        }

        public String getZoneIdSpecification() {
            return zoneIdSpecification;
        }

        public void setZoneIdSpecification(String zoneIdSpecification) {
            this.zoneIdSpecification = zoneIdSpecification;
        }

        public ZoneId getZoneId() {
            switch (getZoneIdSpecification()){
                case "Europe/Budapest" :
                    this.zoneId = ZoneId.of(getZoneIdSpecification());
                    return zoneId;
                default:
                    throw new RuntimeException("Unsupported zone: " + getZoneIdSpecification());
            }
        }

        public void setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        public long getTimerInSecond(){
            return timerInSecond;
        }

        public void setTimerInSecond(long timerInSecond) {
            this.timerInSecond = timerInSecond;
        }
    }

}