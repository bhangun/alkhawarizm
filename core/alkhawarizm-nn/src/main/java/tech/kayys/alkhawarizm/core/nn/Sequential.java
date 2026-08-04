package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;
import java.util.ArrayList;
import java.util.List;

/**
 * Sequential container that chains modules together.
 */
public class Sequential extends Module {

    private final List<Module> moduleList = new ArrayList<>();

    public Sequential() {
    }

    public Sequential(Module... modules) {
        for (int i = 0; i < modules.length; i++) {
            add(String.valueOf(i), modules[i]);
        }
    }

    public Sequential add(String name, Module module) {
        registerModule(name, module);
        moduleList.add(module);
        return this;
    }

    public Sequential add(Module module) {
        return add(String.valueOf(moduleList.size()), module);
    }

    @Override
    public Tensor forward(Tensor input) {
        Tensor output = input;
        for (Module module : moduleList) {
            output = module.forward(output);
        }
        return output;
    }

    public Module get(int index) {
        return moduleList.get(index);
    }

    public int size() {
        return moduleList.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Sequential(\n");
        for (int i = 0; i < moduleList.size(); i++) {
            sb.append("  (").append(i).append("): ")
                    .append(moduleList.get(i))
                    .append("\n");
        }
        sb.append(")");
        return sb.toString();
    }
}
