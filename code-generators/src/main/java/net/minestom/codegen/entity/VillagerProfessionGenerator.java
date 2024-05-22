package net.minestom.codegen.entity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.javapoet.*;
import net.minestom.codegen.MinestomCodeGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minestom.codegen.util.GenerationHelper;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static net.minestom.codegen.util.GenerationHelper.*;

@ApiStatus.NonExtendable
@ApiStatus.Internal
public final class VillagerProfessionGenerator extends MinestomCodeGenerator {

    private static final String VILLAGER_PROFESSION_DATE = "villagerProfessionData";
    private static final Logger LOGGER = LoggerFactory.getLogger(VillagerProfessionGenerator.class);
    private final InputStream villagerProfessionsFile;
    private final File outputFolder;


    public VillagerProfessionGenerator(@Nullable InputStream villagerProfessionsFile, @NotNull File outputFolder) {
        super("");
        this.villagerProfessionsFile = villagerProfessionsFile;
        this.outputFolder = outputFolder;
    }

    @Override
    public void generate() {
        if (villagerProfessionsFile == null) {
            LOGGER.error("Failed to find villager_professions.json.");
            LOGGER.error("Stopped code generation for villager professions.");
            return;
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            LOGGER.error("Output folder for code generation does not exist and could not be created.");
            return;
        }
        // Important classes we use alot
        ClassName rawVillagerProfessionDataClassName = ClassName.get("net.minestom.server.raw_data", "RawVillagerProfessionData");

        JsonArray villagerProfessions = GSON.fromJson(new InputStreamReader(villagerProfessionsFile), JsonArray.class);
        ClassName villagerProfessionClassName = ClassName.get("net.minestom.server.entity.metadata.villager", "VillagerProfession");

        // Particle
        TypeSpec.Builder villagerProfessionClass = TypeSpec.classBuilder(villagerProfessionClassName)
                .addSuperinterface(KEYORI_ADVENTURE_KEY)
                .addModifiers(Modifier.PUBLIC).addJavadoc("AUTOGENERATED by " + getClass().getSimpleName());
        villagerProfessionClass.addField(
                FieldSpec.builder(NAMESPACE_ID_CLASS, "id")
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL).addAnnotation(NotNull.class).build()
        );
        villagerProfessionClass.addField(
                FieldSpec.builder(rawVillagerProfessionDataClassName, VILLAGER_PROFESSION_DATE)
                        .addModifiers(Modifier.PRIVATE, Modifier.VOLATILE)
                        .addAnnotation(NotNull.class)
                        .build()
        );
        villagerProfessionClass.addMethod(
                MethodSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder(NAMESPACE_ID_CLASS, "id").addAnnotation(NotNull.class).build())
                        .addParameter(ParameterSpec.builder(rawVillagerProfessionDataClassName, VILLAGER_PROFESSION_DATE).addAnnotation(NotNull.class).build())
                        .addStatement(VARIABLE_SETTER, "id")
                        .addStatement(VARIABLE_SETTER, VILLAGER_PROFESSION_DATE)
                        .addModifiers(Modifier.PROTECTED)
                        .build()
        );
        // Override key method (adventure)
        villagerProfessionClass.addMethod(GenerationHelper.ADVENTURE_KEY_METHOD);
        // getId method
        villagerProfessionClass.addMethod(GenerationHelper.ID_GETTER);
        // getVillagerProfessionData method
        villagerProfessionClass.addMethod(
                MethodSpec.methodBuilder("getVillagerProfessionData")
                        .returns(rawVillagerProfessionDataClassName)
                        .addAnnotation(NotNull.class)
                        .addStatement("return this.villagerProfessionData")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .build()
        );
        // setVillagerProfessionData method
        villagerProfessionClass.addMethod(
                MethodSpec.methodBuilder("setVillagerProfessionData")
                        .addParameter(ParameterSpec.builder(rawVillagerProfessionDataClassName, VILLAGER_PROFESSION_DATE).addAnnotation(NotNull.class).build())
                        .addStatement("this.$L1 = $L1", VILLAGER_PROFESSION_DATE)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .build()
        );
        // getNumericalId
        villagerProfessionClass.addMethod(
                MethodSpec.methodBuilder("getNumericalId")
                        .returns(TypeName.INT)
                        .addStatement("return $T.VILLAGER_PROFESSION_REGISTRY.getId(this)", REGISTRY_CLASS)
                        .addModifiers(Modifier.PUBLIC)
                        .build()
        );
        // fromId Method
        villagerProfessionClass.addMethod(
                MethodSpec.methodBuilder("fromId")
                        .returns(villagerProfessionClassName)
                        .addAnnotation(Nullable.class)
                        .addParameter(TypeName.INT, "id")
                        .addStatement("return $T.VILLAGER_PROFESSION_REGISTRY.get((short) id)", REGISTRY_CLASS)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build()
        );
        // fromId Method
        villagerProfessionClass.addMethod(
                MethodSpec.methodBuilder("fromId")
                        .returns(villagerProfessionClassName)
                        .addAnnotation(NotNull.class)
                        .addParameter(ADVENTURE_KEY, "id")
                        .addStatement("return $T.VILLAGER_PROFESSION_REGISTRY.get(id)", REGISTRY_CLASS)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build()
        );
        // toString method
        villagerProfessionClass.addMethod(GenerationHelper.TO_STRING);
        // values method
        villagerProfessionClass.addMethod(
                MethodSpec.methodBuilder("values")
                        .addAnnotation(NotNull.class)
                        .returns(ParameterizedTypeName.get(ClassName.get(List.class), villagerProfessionClassName))
                        .addStatement("return $T.VILLAGER_PROFESSION_REGISTRY.values()", REGISTRY_CLASS)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .build()
        );
        CodeBlock.Builder staticBlock = CodeBlock.builder();
        // Use data
        for (JsonElement vp : villagerProfessions) {
            JsonObject villagerProfession = vp.getAsJsonObject();

            String villagerProfessionName = villagerProfession.get("name").getAsString();
            JsonElement workSound = villagerProfession.get("workSound");
            if (workSound == null) {
                villagerProfessionClass.addField(
                        FieldSpec.builder(
                                villagerProfessionClassName,
                                villagerProfessionName
                        ).initializer(
                                "new $T($T.from($S), new $T(() -> null))",
                                villagerProfessionClassName,
                                NAMESPACE_ID_CLASS,
                                villagerProfession.get("id").getAsString(),

                                rawVillagerProfessionDataClassName
                        ).addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).build()
                );
            } else {
                villagerProfessionClass.addField(
                        FieldSpec.builder(
                                villagerProfessionClassName,
                                villagerProfessionName
                        ).initializer(
                                "new $T($T.from($S), new $T(() -> $T.SOUND_EVENT_REGISTRY.get($S)))",
                                villagerProfessionClassName,
                                NAMESPACE_ID_CLASS,
                                villagerProfession.get("id").getAsString(),

                                rawVillagerProfessionDataClassName,
                                REGISTRY_CLASS,
                                workSound.getAsString()
                        ).addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL).build()
                );
            }

            // Add to static init.
            staticBlock.addStatement("$T.VILLAGER_PROFESSION_REGISTRY.register($N)", REGISTRY_CLASS, villagerProfessionName);
        }

        villagerProfessionClass.addStaticBlock(staticBlock.build());

        // Write files to outputFolder
        writeFiles(
                List.of(
                        JavaFile.builder("net.minestom.server.entity.metadata.villager", villagerProfessionClass.build())
                                .indent(DEFAULT_INDENT)
                                .skipJavaLangImports(true)
                                .build()
                ),
                outputFolder
        );
    }
}
