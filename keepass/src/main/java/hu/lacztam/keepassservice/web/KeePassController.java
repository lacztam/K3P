package hu.lacztam.keepassservice.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hu.lacztam.keepassservice.dto.KdbxFileDto;
import hu.lacztam.keepassservice.mapper.KeePassModelMapper;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.service.postgres.KeePassService;
import hu.lacztam.keepassservice.dto.KeePassModelDto;
import hu.lacztam.token.UserDetailsFromJwtToken;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/api/kdbx/{kdbxFileId}/keepass")
public class KeePassController {

    private final KeePassService keePassService;
    private final KeePassModelMapper keePassModelMapper;
    private final UserDetailsFromJwtToken userDetailsFromJwtToken;

    // TO-DO: refactor to import kdbx files into the user main kdbx file!
    @PostMapping("/upload")
    //public ResponseEntity<K3PModelDto> uploadKdbxToDatabase(
    public long uploadKdbxToDatabase(
            @RequestPart("file") MultipartFile file,
            @RequestPart String kdbxFileDtoStr,
            @PathVariable long kdbxFileId,
            HttpServletRequest request) {

        KdbxFileDto kdbxFileDto = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            kdbxFileDto = objectMapper.readValue(kdbxFileDtoStr, KdbxFileDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        kdbxFileDto.setKdbxFileIdDto(kdbxFileId);

        if (kdbxFileDto != null) {

            KeePassModel keePassModel = keePassService.mapRawKdbxFileIntoKeePassModel(file, kdbxFileDto.getKdbxFilePwDto());

            keePassModel = keePassService.save(keePassModel);
            String email = userDetailsFromJwtToken.getUserDetailsFromJwtToken(request).getUsername();
            keePassModel.setEmail(email);
            KeePassModelDto keePassModelDto = keePassModelMapper.keePassModelToDto(keePassModel);
            long kdbxFileIdDto = keePassModelDto.getKdbxFileIdDto();
            return kdbxFileIdDto;

            //return ResponseEntity.ok(k3PModelDto);
        } else {
            throw new NullPointerException("\nuploadKdbxToDatabase()");
        }

    }

    //TO-DO: return value
    @PostMapping("/modify-password")
    public List<String> modifyKDBXFilePassword(
            @RequestBody KdbxFileDto kdbxFileDto,
            HttpServletRequest request) {

        KeePassModel keePassModel = keePassService.changeKDBXFilePassword(
                kdbxFileDto.getKdbxFileIdDto(),
                kdbxFileDto.getKdbxFilePwDto(),
                kdbxFileDto.getNewPasswordDto(),
                request);

        return Arrays.asList(
                "ID: " + keePassModel.getId(),
                "kdbx file name: " + keePassModel.getRedisId(),
                "owner: " + keePassModel.getEmail(),
                "status: pw changed");
    }

    @PutMapping("/rename")
    public boolean renameKeePassDatabase(
            @PathVariable long kdbxFileId,
            @NotNull @RequestBody KdbxFileDto kdbxFileDto) {
        kdbxFileDto.setKdbxFileIdDto(kdbxFileId);

        boolean result = keePassService.modifyKeePassFileDatabaseName(kdbxFileDto);

        return result;
    }

}