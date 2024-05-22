package net.minestom.codegen.biome;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import javax.lang.model.element.Modifier;
import net.minestom.codegen.MinestomCodeGenerator;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApiStatus.Internal
@ApiStatus.NonExtendable
public final class BiomeGenerator extends MinestomCodeGenerator {

    private static final String BIOME_KEY = "biome";
    private static final String BIOME_FILE_PACKAGE = "net.minestom.server.world.biomes"; // Microtus - Biomes
    private static final String CLASS_NAME = "Biomes"; // Microtus - Biomes
    private static final Logger LOGGER = LoggerFactory.getLogger(BiomeGenerator.class);
    private final InputStream biomeFile;
    private final File outputFolder;

    public BiomeGenerator(@Nullable InputStream biomeFile, @NotNull File outputFolder) {
        super(BIOME_FILE_PACKAGE);
        this.biomeFile = biomeFile;
        this.outputFolder = outputFolder;
    }

    @Override
    public void generate() {
        if (biomeFile == null) {
            LOGGER.error("Failed to find biome.json.");
            LOGGER.error("Stopped code generation for biomes.");
            return;
        }
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            LOGGER.error("Output folder for code generation does not exist and could not be created.");
            return;
        }

        JsonElement biomes = GSON.fromJson(new InputStreamReader(biomeFile), JsonElement.class);
        ClassName biomeCN = ClassName.get(BIOME_FILE_PACKAGE, CLASS_NAME);
        ClassName biomeCn = ClassName.get(BIOME_FILE_PACKAGE, "Biome");
        ClassName categoryCn = biomeCn.nestedClass("Category");
        ClassName minecraftServerCn = ClassName.get("net.minestom.server", "MinecraftServer");
        ClassName namespaceIDCn = ClassName.get("net.minestom.server.utils", "NamespaceID");
        ClassName biomeEffectsCn = ClassName.get(BIOME_FILE_PACKAGE, "BiomeEffects");
        // Dye Color Enum
        TypeSpec.Builder biomesEnum = TypeSpec.enumBuilder(biomeCN)
                .addModifiers(Modifier.PUBLIC).addJavadoc("AUTOGENERATED by " + getClass().getSimpleName());

        // Fields
        biomesEnum.addFields(
                List.of(
                        FieldSpec.builder(biomeCn, BIOME_KEY, Modifier.PRIVATE, Modifier.FINAL).build(),
                        FieldSpec.builder(ArrayTypeName.of(biomeCN), "VALUES", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer(CLASS_NAME + ".values()").build()  // Microtus - Banner and shield meta
                )
        );

        // Methods
        biomesEnum.addMethods(
                List.of(
                        // Constructor
                        MethodSpec.constructorBuilder()
                                .addParameter(
                                        ParameterSpec.builder(biomeCn, BIOME_KEY).addAnnotation(NotNull.class).build()
                                )
                                .addStatement("this.$1L = $1L", BIOME_KEY)
                                .build(),
                        MethodSpec.methodBuilder(BIOME_KEY)
                                .addModifiers(Modifier.PUBLIC)
                                .returns(biomeCn.annotated(AnnotationSpec.builder(NotNull.class).build()))
                                .addStatement("return this.$L", BIOME_KEY)
                                .build(),
                        MethodSpec.methodBuilder("getValue")
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addParameter(ParameterSpec.builder(TypeName.INT, "id").build())
                                .addAnnotation(Nullable.class)
                                .returns(ClassName.get(BIOME_FILE_PACKAGE, CLASS_NAME))
                                .addCode("return VALUES[$L];", "id")
                                .build(),
                        MethodSpec.methodBuilder("registerBiomes")
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .beginControlFlow("for (var $L : VALUES)", BIOME_KEY)
                                .addStatement("$1T.getBiomeManager().addBiome($2L.$2L())", minecraftServerCn, BIOME_KEY)
                                .endControlFlow()
                                .build()
                )
        );
        final JsonObject biomesAsJsonObject = biomes.getAsJsonObject();
        biomesAsJsonObject.keySet().forEach(biome -> {
            var biomeObject = biomesAsJsonObject.get(biome).getAsJsonObject();
            var temperature = biomeObject.get("temperature").getAsFloat();
            var downfall = biomeObject.get("downfall").getAsFloat();
            var hasPrecipitation = biomeObject.get("has_precipitation").getAsBoolean();
            var fogColor = Integer.toHexString(new Color(biomeObject.get("fogColor").getAsInt()).getRGB()).substring(2);
            var waterColor = Integer.toHexString(new Color(biomeObject.get("waterColor").getAsInt()).getRGB()).substring(2);
            var waterFogColor = Integer.toHexString(new Color(biomeObject.get("waterFogColor").getAsInt()).getRGB()).substring(2);
            var skyColor = Integer.toHexString(new Color(biomeObject.get("skyColor").getAsInt()).getRGB()).substring(2);
            var effectCodeBlock = CodeBlock.builder();
            effectCodeBlock = effectCodeBlock.add("$T.builder().fogColor(0x$L).waterColor(0x$L).waterFogColor(0x$L).skyColor(0x$L)", biomeEffectsCn, fogColor, waterColor, waterFogColor, skyColor);
            if (biomeObject.has("grassColor")) {
                var grassColor = Integer.toHexString(new Color(biomeObject.get("grassColor").getAsInt()).getRGB()).substring(2);
                effectCodeBlock = effectCodeBlock.add(".grassColor(0x$L)", grassColor);
            }
            if (biomeObject.has("foliageColor")) {
                var foliageColor = Integer.toHexString(new Color(biomeObject.get("foliageColor").getAsInt()).getRGB()).substring(2);
                effectCodeBlock = effectCodeBlock.add(".foliageColor(0x$L)", foliageColor);
            }
            effectCodeBlock = effectCodeBlock.add(".build()");
            var namespacedCode = CodeBlock.of("$T.from($S)", namespaceIDCn, biome.replaceFirst("minecraft:", ""));
            biomesEnum.addEnumConstant(extractNamespace(biome), TypeSpec.anonymousClassBuilder(
                            "$T.builder().name($L).category($T.NONE).depth(0.125F).scale(0.05F).temperature($Lf).downfall($Lf).showPrecipitation($L).effects($L).build()",
                            biomeCn, namespacedCode, categoryCn, temperature, downfall, hasPrecipitation, effectCodeBlock.build()
                    ).build()
            );
        });

        // Write files to outputFolder
        writeFiles(
                List.of(
                        JavaFile.builder(BIOME_FILE_PACKAGE, biomesEnum.build())
                                .indent(DEFAULT_INDENT)
                                .skipJavaLangImports(true)
                                .build()
                ),
                outputFolder
        );
    }
}
