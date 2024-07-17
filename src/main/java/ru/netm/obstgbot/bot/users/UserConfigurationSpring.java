package ru.netm.obstgbot.bot.users;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "")
public class UserConfigurationSpring {
    private Map<String, List<String>> users;
}
