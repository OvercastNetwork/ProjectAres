package tc.oc.document;

import tc.oc.api.annotations.Serialize;

@Serialize
public class ClassDoc {
    public int number;
    public String text;

    public ClassDoc(int number, String text) {
        this.number = number;
        this.text = text;
    }

    public ClassDoc() {}
}
