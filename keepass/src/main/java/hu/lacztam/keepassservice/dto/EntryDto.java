package hu.lacztam.keepassservice.dto;

import de.slackspace.openkeepass.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

@NotNull
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EntryDto {

    @NotNull
    @NotEmpty
    private UUID entryUuidDto;
    private String entryTitleDto;
    private String entryUsernameDto;
    private String entryPasswordDto;
    private String entryTagsDto;
    private String entryNotesDto;
    private String entryUrlDto;
    private Calendar entryExpiryTimeDto;
    private Boolean expiresDto;
    private String targetGroupDirectionDto;

    public static List<String> stringTagToArray(String tags) {
        if (tags == null) {
            return new ArrayList();
        } else {
            String[] splittedTags = tags.split(";");
            List<String> result = new ArrayList();

            if (splittedTags != null) {
                for(int i = 0; i < splittedTags.length; i++){
                    splittedTags[i] = splittedTags[i].strip();
                }

                for(int i = 0; i < splittedTags.length; ++i) {
                    String tag = splittedTags[i];
                    result.add(tag);
                }
            }
            Collections.sort(result);
            return result;
        }
    }

    public static String tagArrayToString(List<String> tags) {
        Collections.sort(tags);
        return tags == null ? null : StringUtils.join(tags, ";");
    }

}
