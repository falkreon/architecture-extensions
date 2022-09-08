package io.github.woodiertexas.architecture_extensions.blocks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

import net.minecraft.block.Block;

public class BlockGroup {
    //General setting info
    protected String name;
    protected Block baseBlock;

    //Standard / ubiquitous blocks
    protected ArchBlock arch;
    protected ColumnBlock column;
    protected RoofBlock roof;
    protected WallPostBlock post;

    //Joists & Moldings
    protected JoistBlock joist;
    protected MoldingBlock crownMolding;

    //Beams & Fence Posts
    protected BeamBlock beam;
    protected FencePostBlock fencePost;

    //Post Caps & Lanterns
    protected PostCapBlock postCap;
    protected PostLanternBlock postLantern;

    protected HashMap<String, Block> others = new HashMap<>();


    private BlockGroup(String name, Block baseBlock) {
        this.name = name;
        this.baseBlock = baseBlock;
    }


    //General setting info
    public String getName() {
        return this.name;
    }

    public Block getBaseBlock() {
        return this.baseBlock;
    }

    //Standard / ubiquitous blocks
    @Nullable
    public ArchBlock getArch() {
        return this.arch;
    }

    @Nullable
    public ColumnBlock getColumn() {
        return this.column;
    }

    @Nullable
    public RoofBlock getRoof() {
        return this.roof;
    }

    @Nullable
    public WallPostBlock getPost() {
        return this.post;
    }

    //Joists & Moldings
    @Nullable
    public JoistBlock getJoist() {
        return this.joist;
    }

    @Nullable
    public MoldingBlock getCrownMolding() {
        return this.crownMolding;
    }

    //Beams & Fence Posts
    @Nullable
    public BeamBlock getBeam() {
        return this.beam;
    }

    @Nullable
    public FencePostBlock getFencePost() {
        return this.fencePost;
    }

    //Post Caps & Lanterns
    @Nullable
    public PostCapBlock getPostCap() {
        return this.postCap;
    }

    @Nullable
    public PostLanternBlock getPostLantern() {
        return this.postLantern;
    }

    public List<Block> getAll() {
        List<Block> result = Stream.of(
                arch, column, roof, post, joist, crownMolding,
                beam, fencePost, postCap, postLantern
                )

                .filter(it->it!=null)
                .collect(Collectors.toList());

        result.addAll(others.values());

        return result;
    }

    public Map<String, Block> getAllMapped() {
        HashMap<String, Block> map = new HashMap<>();
        
        if (arch!=null) map.put(name+"_arch", arch);
        if (column!=null) map.put(name+"_column", column);
        if (roof!=null) map.put(name+"_roof", roof);
        if (post!=null) map.put(name+"_post", post);
        if (joist!=null) map.put(name+"_joist", joist);
        if (crownMolding!=null) map.put(name+"_crown_molding", crownMolding);
        if (beam!=null) map.put(name+"_beam", beam);
        if (fencePost!=null) map.put(name+"_fence_post", fencePost);
        if (postCap!=null) map.put(name+"_post_cap", postCap);
        if (postLantern!=null) map.put(name+"_post_lantern", postLantern);
        
        map.putAll(others);
        
        return map;
    }

    public static Builder builder(String name, Block block) {
        return new Builder(name, block);
    }

    public static class Builder {
        private BlockGroup group;

        public Builder(String name, Block baseBlock) {
            group = new BlockGroup(name, baseBlock);
        }

        public Builder withArch() {
            group.arch = new ArchBlock(group.baseBlock.getDefaultState(), QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withColumn() {
            group.column = new ColumnBlock(QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withRoof() {
            group.roof = new RoofBlock(group.baseBlock.getDefaultState(), QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withPost() {
            group.post = new WallPostBlock(QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withJoist() {
            group.joist = new JoistBlock(QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withCrownMolding() {
            group.crownMolding = new MoldingBlock(group.baseBlock.getDefaultState(), QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withBeam() {
            group.beam = new BeamBlock(QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withFencePost() {
            group.fencePost = new FencePostBlock(QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withPostCap() {
            group.postCap = new PostCapBlock(QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withPostLantern() {
            group.postLantern = new PostLanternBlock(QuiltBlockSettings.copyOf(group.baseBlock).strength(2.5f).requiresTool());
            return this;
        }

        public Builder withBlock(String suffix, Block block) {
            group.others.put(group.name+"_"+suffix, block);
            return this;
        }

        public BlockGroup build() {
            BlockGroup result = this.group;
            this.group = null; //freeze and prevent further mutation
            return result;

        }
    }

}
