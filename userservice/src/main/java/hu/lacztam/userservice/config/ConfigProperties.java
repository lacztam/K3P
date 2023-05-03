package hu.lacztam.userservice.config;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@ConfigurationProperties(prefix = "k3p")
public class ConfigProperties {

    private JwtTokenProperties jwtTokenProperties = new JwtTokenProperties();

    public JwtTokenProperties getJwtTokenProperties() {
        return jwtTokenProperties;
    }

    public void setJwtTokenProperties(JwtTokenProperties jwtTokenProperties) {
        this.jwtTokenProperties = jwtTokenProperties;
    }

    private String appUrl;

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public static class JwtTokenProperties{
        private String auth;
        private String issuer;
        private Date expireTime;
        protected int expireMinute;
        private Algorithm algorithm;
        private String algorithmSpecification;
        private String algorithmSpecificationArgument;

        public Date getExpireTime() {
            this.expireTime
                    = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(this.expireMinute));
            return expireTime;
        }

        public Algorithm getAlgorithm() {

            switch (getAlgorithmSpecification()) {
                case "HMAC256":
                    this.algorithm = Algorithm.HMAC256("HMAC256");
                    break;
                case "HMAC384":
                    this.algorithm = Algorithm.HMAC384("HMAC384");
                    break;
                case "HMAC512":
                    this.algorithm = Algorithm.HMAC512("HMAC512");
                    break;
                default:
                    throw new AlgorithmMismatchException("Unsupported algorithm: "
                            + this.algorithmSpecificationArgument);
            }

            return algorithm;
        }

        public String getAlgorithmSpecification() {
            return algorithmSpecification;
        }
        public String getAlgorithmSpecificationArgument() {
            return algorithmSpecificationArgument;
        }
        public String getIssuer() {
            return issuer;
        }
        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
        public void setExpireTime(Date expireTime) {
            this.expireTime = expireTime;
        }
        public void setAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
        }
        public void setAlgorithmSpecification(String algorithmSpecification) {
            this.algorithmSpecification = algorithmSpecification;
        }
        public void setAlgorithmSpecificationArgument(String algorithmSpecificationArgument) {
            this.algorithmSpecificationArgument = algorithmSpecificationArgument;
        }
        public int getExpireMinute() {
            return expireMinute;
        }
        public void setExpireMinute(int expireMinute) {
            this.expireMinute = expireMinute;
        }
        public String getAuth() {
            return auth;
        }
        public void setAuth(String auth) {
            this.auth = auth;
        }

        @Override
        public String toString() {
            return "JwtTokenProperties{" +
                    "auth='" + auth + '\'' +
                    ", issuer='" + issuer + '\'' +
                    ", expireTime=" + expireTime +
                    ", expireMinute=" + expireMinute +
                    ", algorithm=" + algorithm +
                    ", algorithmSpecification='" + algorithmSpecification + '\'' +
                    ", algorithmSpecificationArgument='" + algorithmSpecificationArgument + '\'' +
                    '}';
        }
    }
}
