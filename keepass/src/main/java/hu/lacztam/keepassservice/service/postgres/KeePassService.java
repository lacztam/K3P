package hu.lacztam.keepassservice.service.postgres;

import de.slackspace.openkeepass.KeePassDatabase;
import de.slackspace.openkeepass.domain.*;
import de.slackspace.openkeepass.domain.zipper.GroupZipper;
import de.slackspace.openkeepass.exception.KeePassDatabaseUnreadableException;
import hu.lacztam.keepassservice.config.ModelType;
import hu.lacztam.keepassservice.dto.KdbxFileDto;
import hu.lacztam.keepassservice.dto.ModifyEntryDto;
import hu.lacztam.keepassservice.dto.ModifyGroupDto;
import hu.lacztam.keepassservice.model.postgres.KeePassModel;
import hu.lacztam.keepassservice.repository.postgres.KeePassRepository;
import hu.lacztam.keepassservice.service.MakeKdbxByteService;
import hu.lacztam.token.JwtService;
import hu.lacztam.token.UserDetailsFromJwtToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class KeePassService {

    @Autowired KeePassRepository keePassRepository;
    @Autowired
    MakeKdbxByteService makeKdbxByteService;
    @Lazy @Autowired
    GroupService_OLD groupServiceOLD;
    @Autowired UserDetailsFromJwtToken userDetailsFromJwtToken;
    @Autowired JwtService jwtService;

    @Transactional
    public KeePassModel save(KeePassModel keePassModel) {
        return keePassRepository.save(keePassModel);
    }

    @Transactional
    public void delete(KeePassModel keePassModel){
        keePassRepository.delete(keePassModel);
    }

    @Transactional
    public KeePassModel findByID(long kdbxFileId) {
        return keePassRepository.findById(kdbxFileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public KeePassModel findByIDWithUserCheck(long kdbxFileId, HttpServletRequest request) {
        String email = userDetailsFromJwtToken.getUserDetailsFromJwtToken(request).getUsername();

        Optional<KeePassModel> optionalK3PModel = keePassRepository.findByIDWithUserCheck(kdbxFileId, email);
        if(optionalK3PModel.isPresent()){
            return optionalK3PModel.get();
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public KeePassModel findByIdNoAttachmentWithUserAuth(long kdbxFileId, HttpServletRequest request){
        String email = userDetailsFromJwtToken.getUserDetailsFromJwtToken(request).getUsername();

        Optional<KeePassModel> optionalK3PModel = keePassRepository.findByIdNoAttachmentWithUserAuth(kdbxFileId, email);
        if(optionalK3PModel.isPresent()){
                return optionalK3PModel.get();

        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public Optional<KeePassModel> findByIDWithAttachmentsWithUserAuth(long kdbxFileId, HttpServletRequest request) {
        String email = userDetailsFromJwtToken.getUserDetailsFromJwtToken(request).getUsername();

        Optional<KeePassModel> optionalK3PModel = keePassRepository.findByIDWithAttachmentsWithUserAuth(kdbxFileId, email);
        if(optionalK3PModel.isPresent()){
            return optionalK3PModel;
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public KeePassModel findByName(String keePassFileName) {
        return keePassRepository.findbyName(keePassFileName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public KeePassModel mapRawKdbxFileIntoKeePassModel(MultipartFile file, String password) {
        KeePassFile keePassFile = null;

        try {
            keePassFile = KeePassDatabase
                    .getInstance(fileByteInputStream(file))
                    .openDatabase(password);
        } catch (KeePassDatabaseUnreadableException e) {
            e.printStackTrace();
        }

        KeePassModel keePassModel = new KeePassModel();
        if(file.getOriginalFilename() == null || file.getOriginalFilename() == ""){
            keePassModel.setRedisId("kdbxFileHasNoName.kdbx");
        }else{
            keePassModel.setRedisId(file.getOriginalFilename());
        }
        keePassModel = save(keePassModel);
        byte[] kdbxInBytes = makeKdbxFileInBytesWithoutAttachments(keePassFile, password);

        keePassModel.setKdbxFile(kdbxInBytes);
        keePassModel = save(keePassModel);

        return keePassModel;
    }

    private InputStream fileByteInputStream(MultipartFile file) {
        InputStream inputStream = null;

        try {
            if (file.getBytes() != null || file.getBytes().length > 0)
                inputStream = new ByteArrayInputStream(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return inputStream;
    }

    public List<Long> getAllIds() {
        return keePassRepository.getAllIds();
    }

    @Transactional
    public String exportKDBXFileWithoutAttachment(KeePassModel keePassModel, String pathToSave) {
        String dbName = keePassModel.getRedisId();
        try {
            Files.write(Path.of(pathToSave + "/" + dbName), keePassModel.getKdbxFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pathToSave + "/" + dbName;
    }

    @Transactional
    public byte[] makeKdbxFileInBytesWithoutAttachments(KeePassFile keePassFile, String password) {
        Group root = groupServiceOLD.mapTopGroupWithoutAttachment(keePassFile.getRoot().getGroups().get(0));

        Meta meta = new MetaBuilder(keePassFile
                .getMeta()
                .getDatabaseName())
                .build();

        KeePassFile uploadKeePass = new KeePassFileBuilder(meta).addTopGroups(root).build();

        byte[] kdbxInBytes = makeKdbxByteService.makeKdbx(uploadKeePass, password);

        return kdbxInBytes;
    }

    @Transactional
    public boolean modifyKeePassFileDatabaseName(KdbxFileDto kdbxFileDto){
        if(kdbxFileDto.getKeePassDatabaseNameDto() == null)
            return false;

        boolean validateFileName = checkIfFileNameIsValid(kdbxFileDto.getKeePassDatabaseNameDto());

        if(validateFileName){
            KeePassModel keePassModel = findByID(kdbxFileDto.getKdbxFileIdDto());
            KeePassFile keePassFile = keePassModel.getKeePassFile(kdbxFileDto.getKdbxFilePwDto());

            Meta metaWithNewDbName = new MetaBuilder(keePassFile.getMeta())
                    .databaseName(kdbxFileDto.getKeePassDatabaseNameDto())
                    .build();

            Group root = groupServiceOLD.mapTopGroupWithoutAttachment(keePassFile.getRoot().getGroups().get(0));
            KeePassFile uploadKeePass = new KeePassFileBuilder(metaWithNewDbName).addTopGroups(root).build();

            byte[] kdbxInBytes = makeKdbxByteService.makeKdbx(uploadKeePass, kdbxFileDto.getKdbxFilePwDto());

            keePassModel.setKdbxFile(kdbxInBytes);
            keePassModel = save(keePassModel);

            return true;
        }else{
            return false;
        }
    }

    @Transactional
    public boolean modifyKdbxFileName(KdbxFileDto kdbxFileDto) {
        if (kdbxFileDto.getKdbxFileNameDto() == null)
            return false;

        boolean validateNewKdbxFileName = checkKdbxFileNameIsValid(kdbxFileDto.getKdbxFileNameDto());
        if (!validateNewKdbxFileName)
            return false;

        KeePassModel keePassModel = findByID(kdbxFileDto.getKdbxFileIdDto());

        String kdbxFileName = kdbxFileDto.getKdbxFileNameDto();
        keePassModel.setRedisId(kdbxFileName);
        keePassModel = save(keePassModel);

        return true;
    }

    public boolean checkKdbxFileNameIsValid(String filename){
        if (filename == null || filename.length() > 255 || filename.isEmpty())
            return false;

        final String REGEX_PATTERN = "^[A-za-z0-9_.-]+.kdbx$";
        return filename.matches(REGEX_PATTERN);
    }

    public boolean checkIfFileNameIsValid(String filename) {
        if (filename == null || filename.length() > 255 || filename.isEmpty())
            return false;

        final String REGEX_PATTERN = "^[A-za-z0-9_.-]{1,255}$";

        return filename.matches(REGEX_PATTERN);
    }

    @Transactional
    public KeePassModel changeKDBXFilePassword(
            long kdbxFileId,
            String oldPassword,
            String newPassword,
            HttpServletRequest request) {

        KeePassModel keePassModel = findByIdNoAttachmentWithUserAuth(kdbxFileId, request);

        KeePassFile keePassFile = keePassModel.getKeePassFile(oldPassword);
        byte[] kdbxFileInBytesWithNewPassword = makeKdbxByteService.makeKdbx(keePassFile, newPassword);
        keePassModel.setKdbxFile(kdbxFileInBytesWithNewPassword);
        keePassModel = save(keePassModel);

        return keePassModel;
    }

    @Transactional
    public KeePassModel uploadModifiedKdbxFile(ModifyGroupDto modifyGroupDto, Group modifiedGroup, HttpServletRequest request) {
        KeePassModel keePassModel = findByIDWithUserCheck(modifyGroupDto.getKdbxFileDto().getKdbxFileIdDto(), request);

        if (keePassModel != null) {

            KeePassFile originalKeePassFile = keePassModel.getKeePassFile(modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());

            KeePassFile modifiedKeePass =
                    new KeePassFileBuilder(originalKeePassFile.getMeta())
                            .addTopGroups(modifiedGroup)
                            .build();

            byte[] keePassFileInBytes
                    = makeKdbxByteService.makeKdbx(modifiedKeePass, modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());

            keePassModel.setKdbxFile(keePassFileInBytes);
            keePassModel = save(keePassModel);

            return keePassModel;
        } else {
            throw new NullPointerException("K3PModel can not be null.");
        }
    }

    @Transactional
    public KeePassModel uploadModifiedKdbxFile(KeePassModel keePassModel, ModifyGroupDto modifyGroupDto, Group modifiedGroup) {
        if (keePassModel != null) {
            KeePassFile originalKeePassFile = keePassModel.getKeePassFile(modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());

            KeePassFile modifiedKeePass =
                    new KeePassFileBuilder(originalKeePassFile.getMeta())
                            .addTopGroups(modifiedGroup)
                            .build();

            byte[] keePassFileInBytes
                    = makeKdbxByteService.makeKdbx(modifiedKeePass, modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());

            keePassModel.setKdbxFile(keePassFileInBytes);
            keePassModel = save(keePassModel);

            return keePassModel;
        } else {
            throw new NullPointerException("K3PModel can not be null.");
        }
    }

    @Transactional
    public KeePassModel uploadModifiedKdbxFile(KeePassModel keePassModel, ModifyEntryDto modifyEntryDto, Group modifiedGroup) {
        if (keePassModel != null) {
            KeePassFile originalKeePassFile = keePassModel.getKeePassFile(modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto());

            KeePassFile modifiedKeePass =
                    new KeePassFileBuilder(originalKeePassFile.getMeta())
                            .addTopGroups(modifiedGroup)
                            .build();

            byte[] keePassFileInBytes
                    = makeKdbxByteService.makeKdbx(modifiedKeePass, modifyEntryDto.getKdbxFileDto().getKdbxFilePwDto());

            keePassModel.setKdbxFile(keePassFileInBytes);
            keePassModel = save(keePassModel);

            return keePassModel;
        } else {
            throw new NullPointerException("K3PModel can not be null.");
        }
    }

    @Transactional
    public KeePassModel uploadModifiedKdbxFile_modifyGroup(KeePassModel keePassModel, ModifyGroupDto modifyGroupDto, Group originalGroup){
        if (keePassModel != null) {
            KeePassFile keePassFile = keePassModel.getKeePassFile(modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());

            TimesBuilder timesBuilder = null;
            Group modifyGroupName = null;
            if(originalGroup.getTimes() != null){
                timesBuilder = new TimesBuilder(originalGroup.getTimes());
                if (modifyGroupDto.getGroupDto().isExpiresDto()) {
                    timesBuilder.expires(modifyGroupDto.getGroupDto().isExpiresDto());
                    timesBuilder.expiryTime(modifyGroupDto.getGroupDto().getModifyGroupExpiryTimeDto());
                }

                modifyGroupName = new GroupBuilder(originalGroup)
                        .name(modifyGroupDto.getGroupDto().getGroupNameDto())
                        .times(timesBuilder.build())
                        .addEntries(originalGroup.getEntries())
                        .build();
            }else{
                modifyGroupName = new GroupBuilder(originalGroup)
                        .name(modifyGroupDto.getGroupDto().getGroupNameDto())
                        .addEntries(originalGroup.getEntries())
                        .build();
            }

            GroupZipper groupZipper = new GroupZipper(keePassFile);
            groupZipper = groupZipper.replace(modifyGroupName);
            KeePassFile modifiedKeePassFile = groupZipper.close();
            byte[] keePassFileInBytes = makeKdbxByteService.makeKdbx(modifiedKeePassFile, modifyGroupDto.getKdbxFileDto().getKdbxFilePwDto());
            keePassModel.setKdbxFile(keePassFileInBytes);
            keePassModel = save(keePassModel);

            return keePassModel;
        }else{
            throw new NullPointerException("K3PModel can not be null.");
        }
    }

    public KeePassModel findMainKeePassByUserEmail(String email) {
        String redisId = email + ModelType.MAIN_KEEPASS;
        Optional<KeePassModel> optionalKeePassModel = keePassRepository.findMainByRedisId(redisId);

        if(optionalKeePassModel.isPresent()){
            return optionalKeePassModel.get();
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    public boolean isMainKeePassAlreadyExists(String email){
        String redisId = email + ModelType.MAIN_KEEPASS;
        Optional<KeePassModel> optionalKeePassModel = keePassRepository.findMainByRedisId(redisId);
        return optionalKeePassModel.isPresent() ? true : false;
    }

    public boolean isSharedKeePassAlreadyExists(String email){
        String redisId = email + ModelType.SHARED_KEEPASS;
        Optional<KeePassModel> optionalKeePassModel = keePassRepository.findMainByRedisId(redisId);
        return optionalKeePassModel.isPresent() ? true : false;
    }

    public KeePassModel findSharedKeePassByUserEmail(String email) {
        String redisId = email + ModelType.SHARED_KEEPASS;
        Optional<KeePassModel> optionalKeePassModel = keePassRepository.findSharedByRedisId(redisId);

        if(optionalKeePassModel.isPresent()){
            return optionalKeePassModel.get();
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

}