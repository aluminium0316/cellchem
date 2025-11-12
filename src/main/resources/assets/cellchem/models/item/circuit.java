import java.io.PrintWriter;

public class circuit {
    public static void main(String[] args) throws Throwable {
        for (int i = 1; i < 33; i++) {
            PrintWriter writer = new PrintWriter("circuit" + i + ".json", "UTF-8");
            writer.println("{\n" +
                    "  \"parent\": \"item/generated\",\n" +
                    "  \"textures\": {\n" +
                    "    \"layer0\": \"cellchem:items/circuit" + i + "\"\n" +
                    "  }\n" +
                    "}\n");
            writer.close();
        }
    }
}