package hu.lacztam.keepassservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@NotNull
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModifyEntryDto {
    @NotNull
    private KdbxFileDto kdbxFileDto;
    @NotNull
    private EntryDto entryDto;
}