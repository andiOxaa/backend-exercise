package models.content;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.bson.codecs.pojo.annotations.BsonDiscriminator;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@BsonDiscriminator(key = "type",value = "LINE")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LineContent extends BaseContent implements Content{

    @NotEmpty
    private List<Datas> data;

    @Override
    public @NotNull Type getType() {
        return Type.LINE;
    }
}