package hu.lacztam.keepassservice.mapper;

import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.EntryBuilder;
import de.slackspace.openkeepass.domain.TimesBuilder;
import hu.lacztam.keepassservice.dto.EntryDto;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.TimeZone;

@Component
public class EntryMapper {

    //TO-DO: stream
    public EntryDto entryToDtoWithoutAttachmentData(Entry entry){
        EntryDto entryDto = new EntryDto();
        entryDto.setEntryUuidDto(entry.getUuid());
        entryDto.setEntryUrlDto(entry.getUrl());
        entryDto.setEntryTitleDto(entry.getTitle());
        entryDto.setEntryUsernameDto(entry.getUsername());
        entryDto.setEntryPasswordDto(entry.getPassword());
        entryDto.setEntryExpiryTimeDto(entry.getTimes().getExpiryTime());
        if(entry.getTags() != null){
            entryDto.setEntryTagsDto(entryDto.tagArrayToString(entry.getTags()));
        }
        entryDto.setEntryNotesDto(entry.getNotes());

        return entryDto;
    }

    public Entry dtoToEntry(EntryDto entryDto){
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        TimesBuilder timesBuilder = new TimesBuilder()
                .expires(entryDto.getExpiresDto())
                .expiryTime(entryDto.getEntryExpiryTimeDto())
                .creationTime(calendar);

        EntryBuilder entryBuilder = new EntryBuilder()
                .title(entryDto.getEntryTitleDto())
                .username(entryDto.getEntryUsernameDto())
                .password(entryDto.getEntryPasswordDto())
                .tags(EntryDto.stringTagToArray(entryDto.getEntryTagsDto()))
                .notes(entryDto.getEntryNotesDto())
                .url(entryDto.getEntryUrlDto())
                .times(timesBuilder.build());

        return entryBuilder.build();
    }
}
