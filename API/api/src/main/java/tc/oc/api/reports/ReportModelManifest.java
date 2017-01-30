package tc.oc.api.reports;

import tc.oc.api.docs.Report;
import tc.oc.api.docs.virtual.ReportDoc;
import tc.oc.api.model.ModelBinders;
import tc.oc.commons.core.inject.HybridManifest;

public class ReportModelManifest extends HybridManifest implements ModelBinders {

    @Override
    protected void configure() {
        bindModel(Report.class, ReportDoc.Partial.class, model -> {
            model.bindDefaultService().to(model.nullService());
        });
    }
}
