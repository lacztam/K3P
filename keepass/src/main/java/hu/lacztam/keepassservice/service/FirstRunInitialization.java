package hu.lacztam.keepassservice.service;

import hu.lacztam.keepassservice.service.postgres.KeePassService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class FirstRunInitialization {

    private final KeePassService keePassService;
    private final MakeKdbxByteService makeKdbxByteService;

    private final String empty =
            "\n-----------------------------------------------\n" +
                    "### The database is empty.\n" +
                    "Initialization in progress.." +
                    "\n-----------------------------------------------";
    private final String init =
            "\n-----------------------------------------------\n" +
                    "### Initialization completed successfully. ###" +
                    "\n-----------------------------------------------\n";

    public void checkDatabase() {

    }

    private void initialization() {

    }
}
