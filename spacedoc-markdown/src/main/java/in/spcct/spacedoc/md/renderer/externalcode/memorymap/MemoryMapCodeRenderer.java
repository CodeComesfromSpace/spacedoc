package in.spcct.spacedoc.md.renderer.externalcode.memorymap;

import in.spcct.spacedoc.common.exception.RenderingException;
import in.spcct.spacedoc.md.renderer.ExternalCodeRenderer;
import in.spcct.spacedoc.md.renderer.bitfield.BitFieldRenderer;
import in.spcct.spacedoc.md.renderer.bitfield.ConfigUtils;
import in.spcct.spacedoc.md.renderer.bitfield.fieldtype.FieldType;
import in.spcct.spacedoc.md.renderer.bitfield.fieldtype.Register;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A renderer for a simplified memory map, using {@link BitFieldRenderer} as a backend.
 * <p>
 * The "memory-map" DSL accepts source in the following format, provided by an example:
 * <pre>
 * [0x0000 - 0x0000](6) label   // continuous, taking up 6 lines
 * [0x0000 - 0x0000] label      // continuous, taking up a default amount of lines
 * [0x0000 ~ 0xFFFF](6) label   // broken, taking up 6 lines
 * [0x0000 ~ 0xFFFF] label      // broken, taking up a default amount of lines
 * [0x0000]          label      // single entry, taking up a single line
 *
 * [0xBEEF]          It's beef! // And this is a comment
 *
 * -- array-width: 16
 * -- default-large-array-size: 4
 *
 * -- key: value
 * </pre>
 * <p>
 * Entries may specify either a single memory address, or an address range.
 * <p>
 * Address ranges provided with a dash (-) separator will be rendered as solid blocks.
 * Address ranges provided with a tilde (~) separator will be rendered as a "broken" block.
 * <p>
 * The config value "array-width" specifies the number of bits the array should take up.
 * The config value "default-large-array-size" specifies the number of lines to be taken up by a block specified by a range of addresses with no size specified.
 * <p>
 * Additional config values for the underlying renderer can be specified,
 * see {@link in.spcct.spacedoc.md.renderer.bitfield.BitFieldRenderer.Config} for more information.
 */
public class MemoryMapCodeRenderer implements ExternalCodeRenderer {

    private static final String CONFIG_ARRAY_WIDTH = "array-width";
    private static final String CONFIG_DEFAULT_LARGE_ARRAY_SIZE = "default-large-array-size";

    @Override
    public String languageName() {
        return "memory-map";
    }

    @Override
    public String renderSvg(String source) throws RenderingException {

        MemoryMapParser parser = new MemoryMapParser();

        parser.parse(source);

        Map<String,String> config = parser.getConfigMap();

        BitFieldRenderer.Config rendererConfig = ConfigUtils.createRendererConfig(config);

        BitFieldRenderer bitFieldRenderer = new BitFieldRenderer(rendererConfig);

        List<MemoryMapEntry> entries = parser.getEntryList();


        final int arrayWidth;

        if(config.containsKey(CONFIG_ARRAY_WIDTH)) {
            arrayWidth = Integer.parseInt(config.get(CONFIG_ARRAY_WIDTH));
        }else {
            arrayWidth = 8; //random default
        }

        final int defaultLargeArraySize;
        if(config.containsKey(CONFIG_DEFAULT_LARGE_ARRAY_SIZE)) {
            defaultLargeArraySize = Integer.parseInt(config.get(CONFIG_DEFAULT_LARGE_ARRAY_SIZE));
        }else {
            defaultLargeArraySize = 3; //random default
        }

        return bitFieldRenderer.renderStuff(
                entries.stream()
                        .map(e -> map(e, arrayWidth, defaultLargeArraySize))
                        .collect(Collectors.toList())
        );

    }

    private FieldType map(MemoryMapEntry memoryMapEntry, int arrayWidth, int defaultLargeArraySize) {
        Register.RegisterBuilder builder = Register.builder()
                .centerRightLabel(memoryMapEntry.getLabel())
                .breakInTheMiddle(memoryMapEntry.getIsBroken())
                .bitArrays(
                        Collections.singletonList(
                                new Register.BitArray(
                                        null,
                                        0,  //TODO: colors
                                        arrayWidth
                                )
                        )
                );

        if(memoryMapEntry.getEndingAddress() == null) {
            builder = builder.centerLeftLabel(memoryMapEntry.getStartingAddress());
        }else{
            builder = builder
                    .topLeftLabel(memoryMapEntry.getStartingAddress())
                    .bottomLeftLabel(memoryMapEntry.getEndingAddress());
        }

        if (memoryMapEntry.isRange() && memoryMapEntry.getSize() == null) {
            memoryMapEntry.setSize(defaultLargeArraySize);
        }

        if(memoryMapEntry.getSize() == null) {
            memoryMapEntry.setSize(1);
        }

        return builder
                .numberOfRows(memoryMapEntry.getSize())
                .build();
    }
}
