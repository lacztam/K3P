package hu.lacztam.keepassservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {

    private boolean expiresDto;
    private Calendar modifyGroupExpiryTimeDto;
    private String targetGroupDirectionDto;
    private String sourceGroupDirectionDto;
    private String notesDto;
    private String groupNameDto;
    private boolean expandedDto;

    private Calendar creationTimeDto;
    private Calendar expireTimeDto;

}
