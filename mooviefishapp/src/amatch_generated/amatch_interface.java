/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package amatch_generated;

public class amatch_interface implements amatch_interfaceConstants {
  public static String amatch_version() {
    return amatch_interfaceJNI.amatch_version();
  }

  public static int num_samples_to_record() {
    return amatch_interfaceJNI.num_samples_to_record();
  }

  public static double num_sec_to_record() {
    return amatch_interfaceJNI.num_sec_to_record();
  }

  public static int get_sample_rate() {
    return amatch_interfaceJNI.get_sample_rate();
  }

  public static double delay_per_sec() {
    return amatch_interfaceJNI.delay_per_sec();
  }

  public static long read_track_fpkeys(String fn) {
    return amatch_interfaceJNI.read_track_fpkeys(fn);
  }

  public static int generate_fp_keys_from_in() {
    return amatch_interfaceJNI.generate_fp_keys_from_in();
  }

  public static int match_sample() {
    return amatch_interfaceJNI.match_sample();
  }

  public static void get_recorded_samples(float[] p) {
    amatch_interfaceJNI.get_recorded_samples(p);
  }

  public static void put_recorded_samples(short[] p, int size) {
    amatch_interfaceJNI.put_recorded_samples(p, size);
  }

  public static int get_recorded_samples_size() {
    return amatch_interfaceJNI.get_recorded_samples_size();
  }

  public static void clear_recorded_samples() {
    amatch_interfaceJNI.clear_recorded_samples();
  }

  public static void write_recorded_as_file(String fname) {
    amatch_interfaceJNI.write_recorded_as_file(fname);
  }

}