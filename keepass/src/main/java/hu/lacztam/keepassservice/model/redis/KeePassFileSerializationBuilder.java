//package hu.lacztam.keepassservice.model.redis;
//
//import de.slackspace.openkeepass.domain.*;
//
//public class KeePassFileSerializationBuilder implements KeePassFileContract{
//
//    Meta meta;
//    Group root;
//    private GroupBuilder rootBuilder = new GroupBuilder();
//    private GroupBuilder topGroupBuilder = new GroupBuilder();
//
//    public KeePassFileSerializationBuilder(KeePassFile keePassFile) {
//        this.meta = keePassFile.getMeta();
//        this.rootBuilder = new GroupBuilder(keePassFile.getRoot());
//    }
//
//    public KeePassFileSerializationBuilder(String databaseName) {
//        this.meta = (new MetaBuilder(databaseName)).historyMaxItems(10L).build();
//    }
//
//    public KeePassFileSerializationBuilder(Meta meta) {
//        this.meta = meta;
//    }
//
//    public KeePassFileSerializationBuilder withMeta(Meta meta) {
//        this.meta = meta;
//        return this;
//    }
//
//    public KeePassFileSerializationBuilder addTopGroups(Group... groups) {
//        Group[] var2 = groups;
//        int var3 = groups.length;
//
//        for(int var4 = 0; var4 < var3; ++var4) {
//            Group group = var2[var4];
//            this.rootBuilder.addGroup(group);
//        }
//
//        return this;
//    }
//
//    public KeePassFileSerializationBuilder addTopEntries(Entry... entries) {
//        Entry[] var2 = entries;
//        int var3 = entries.length;
//
//        for(int var4 = 0; var4 < var3; ++var4) {
//            Entry entry = var2[var4];
//            this.topGroupBuilder.addEntry(entry);
//        }
//
//        return this;
//    }
//
//    public KeePassFileSerialization build() {
//        this.setTopGroupNameIfNotExisting();
//        this.root = this.rootBuilder.build();
//        return new KeePassFileSerialization(this);
//    }
//
//    private void setTopGroupNameIfNotExisting() {
//        if (this.rootBuilder.getGroups().isEmpty()) {
//            this.rootBuilder.addGroup(this.topGroupBuilder.name(this.meta.getDatabaseName()).build());
//        }
//
//    }
//
//    public Meta getMeta() {
//        return this.meta;
//    }
//
//    public Group getRoot() {
//        return this.root;
//    }
//
//}
