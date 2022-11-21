package org.laoruga.dtogenerator.examples.generators.custom;

import org.laoruga.dtogenerator.api.generators.ICustomGeneratorArgs;
import lombok.AllArgsConstructor;
import org.apache.commons.text.RandomStringGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Il'dar Valitov
 * Created on 15.11.2022
 */
public class CustomGeneratorWithArgs implements ICustomGeneratorArgs<List<String>> {

    Args args;

    @Override
    public ICustomGeneratorArgs<List<String>> setArgs(String... args) {
        this.args = convert(args);
        return this;
    }

    @Override
    public List<String> generate() {
        return IntStream.range(0, args.size).boxed()
                .map(i -> new RandomStringGenerator.Builder()
                        .selectFrom(args.charset).build().generate(0, args.maxLength))
                .collect(Collectors.toList());
    }

    private Args convert(String... args) {
        Args params;
        try {
            int size = Integer.parseInt(args[0]);
            int maxLength = Integer.parseInt(args[1]);
            char[] charset = args[2].toCharArray();
            params = new Args(size, maxLength, charset);
        } catch (Exception e) {
            throw new IllegalArgumentException("Two arguments must be passed, but was: " + Arrays.asList(args));
        }
        if (params.size <= 0) {
            throw new IllegalArgumentException("First argument must be more then 0, but was: " + params.size);
        }
        return params;
    }

    @AllArgsConstructor
    private static class Args {
        int size;
        int maxLength;
        char[] charset;
    }

}
