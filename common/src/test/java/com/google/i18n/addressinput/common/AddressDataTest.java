/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.i18n.addressinput.common;

import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class AddressDataTest {

  @Test public void testAddressLineSimple() {
    AddressData address = AddressData.builder()
        .setAddressLines(asList("First line", "Second line"))
        .build();
    assertThat(address.getAddressLines())
        .containsExactly("First line", "Second line").inOrder();
  }

  @SuppressWarnings("deprecation")
  @Test public void testManyAddressLines() {
    List<String> lines = asList(
        "First line", "Second line", "Third line", "Fourth line", "Last line");
    AddressData address = AddressData.builder()
        .setAddressLines(lines)
        .build();
    assertEquals(lines, address.getAddressLines());

    // When accessing via deprecated line getters, any lines after 2 are concatenated to the second.
    assertEquals("First line", address.getAddressLine1());
    assertEquals("Second line, Third line, Fourth line, Last line", address.getAddressLine2());
  }

  @SuppressWarnings("deprecation")
  @Test public void testSetSingleAddressLine() {
    AddressData address = AddressData.builder().setAddress("Address Line").build();
    assertThat(address.getAddressLines())
        .containsExactly("Address Line").inOrder();

    // When accessing via deprecated line getters, any missing lines return null.
    assertEquals("Address Line", address.getAddressLine1());
    assertNull(address.getAddressLine2());
  }

  @Test public void testAddressLineNormalisationTrimsWhitespace() {
    AddressData address = AddressData.builder()
        .setAddressLines(asList("\t\n  Line with whitespace around it \n  "))
        .build();
    assertThat(address.getAddressLines())
        .containsExactly("Line with whitespace around it").inOrder();
  }

  @Test public void testAddressLineNormalisationSplitsOnNewline() {
    AddressData address = AddressData.builder()
        .setAddressLines(asList("First line\nSecond line")).build();
    assertThat(address.getAddressLines())
        .containsExactly("First line", "Second line").inOrder();
  }

  @Test public void testAddressLineNormalisationExcludesNull() {
    AddressData address = AddressData.builder()
        .setAddressLines(asList(null, "First non-null line"))
        .build();
    assertThat(address.getAddressLines())
        .containsExactly("First non-null line").inOrder();
  }

  @Test public void testAddressLineNormalisationExcludesEmptyOrWhitespace() {
    AddressData address = AddressData.builder()
        .setAddressLines(asList("", "First non-empty line"))
        .build();
    assertThat(address.getAddressLines())
        .containsExactly("First non-empty line").inOrder();
  }

  @Test public void testAddressLineNormalisationComplex() {
    AddressData address = AddressData.builder()
        .setAddressLines(asList("  \n\t \t\n", "   \n   First non-empty line   \n   \t "))
        .build();
    assertThat(address.getAddressLines())
        .containsExactly("First non-empty line").inOrder();
  }

  @Test public void testAddressLineNormalisationManyEmbeddedLines() {
    AddressData address = AddressData.builder()
        .setAddressLines(asList("\nFirst line  \nSecond line  ", "\n  Third line\n Last line \t "))
        .build();
    assertThat(address.getAddressLines())
        .containsExactly("First line", "Second line", "Third line", "Last line").inOrder();
  }

  @SuppressWarnings("deprecation")
  @Test public void testBuilderSplitsLinesLazily() {
    AddressData address = AddressData.builder()
        .setAddressLines(asList("First line\nSecond line"))
        // The first address line hasn't been split yet, so resetting the 2nd line does nothing.
        .setAddressLine2(null)
        .build();
    assertThat(address.getAddressLines())
        .containsExactly("First line", "Second line").inOrder();
  }

  @SuppressWarnings("deprecation")
  @Test public void testBuilderDoesNotReindexFieldsOnRemoval() {
    AddressData address = AddressData.builder()
        .setAddressLine1("First line (should be removed)")
        .setAddressLine2("Second line (should be removed)")
        // Removing the first line does not reindex the 2nd line to be the first line.
        .setAddressLine1(null)
        .setAddressLine2("Only line")
        .build();
    assertThat(address.getAddressLines())
        .containsExactly("Only line").inOrder();
  }

  @Test public void testNoAdminArea() {
    AddressData address = AddressData.builder().build();
    assertNull(address.getAdministrativeArea());
  }

  @Test public void testSetLanguageCode() throws Exception {
    AddressData address = AddressData.builder()
        .setCountry("TW")
        .setAdminArea("\u53F0\u5317\u5E02")  // Taipei City
        .setLocality("\u5927\u5B89\u5340")  // Da-an District
        .build();
    // Actually, this address is not in Latin-script Chinese, but we are just testing that the
    // language we set is the same as the one we get back.
    address = AddressData.builder(address).setLanguageCode("zh-latn").build();
    assertEquals("zh-latn", address.getLanguageCode());
  }
}
