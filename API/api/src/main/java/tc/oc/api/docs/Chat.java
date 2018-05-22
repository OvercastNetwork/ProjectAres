package tc.oc.api.docs;

import tc.oc.api.annotations.Serialize;
import tc.oc.api.docs.virtual.ChatDoc;

@Serialize
public interface Chat extends ChatDoc.Complete {}
