This branch contains a stripped down version of the [_ApkSignatureKiller_](https://github.com/L-JINBIN/ApkSignatureKiller) utility.

[_config.txt_](https://github.com/warren-bank/fork-java-ApkSignatureKiller/blob/output-signatures-data/dist/config.txt)
* `apk.signed`
  - path to input signed APK file
* `txt.out`
  - path to output text file
    * contains a Base64 encoded string with signatures data

It's useful when working with an APK that has already been decompiled to smali.

#### Usage Example:

1. pre-existing work environment
   ```bash
     app_dir='/path/to/app'
     app_apk="${app_dir}.apk"

     apktool d "$app_apk"
   ```
2. add template
   ```bash
     mkdir -p "${app_dir}/smali/cc/binmt/signature"
     wget -P  "${app_dir}/smali/cc/binmt/signature" "https://github.com/warren-bank/fork-java-ApkSignatureKiller/raw/master/src/cc/binmt/signature/PmsHookApplication.smali"
   ```
3. generate Base64 signatures data
   ```bash
     nkstool_dir='/path/to/nkstool/dist'
     app_txt="${app_dir}.txt"

     echo "apk.signed=${app_apk}" >"${nkstool_dir}/config.txt"
     echo "txt.out=${app_txt}"   >>"${nkstool_dir}/config.txt"

     java -jar "${nkstool_dir}/nkstool.jar"
   ```
4. apply manual edits
   - file: `${app_dir}/AndroidManifest.xml`
     * operation:
       - search: `(<application [^>]+ android:name=)"([^"]+)"`
       - replace: `$1"cc.binmt.signature.PmsHookApplication"`
       - keep: `application_class="L${2//.//};"`
   - file: `${app_dir}/smali/cc/binmt/signature/PmsHookApplication.smali`
     * operation:
       - search: `const-string/jumbo v6, "### Signatures Data ###"`
       - replace: `$(cat "$app_txt")`
     * operation:
       - search: `Landroid/app/Application;`
       - replace: `$application_class`

#### Concrete Usage Example:

* apply manual edits
  - file: `${app_dir}/AndroidManifest.xml`
    * operation:
      - search: `android:name="com.my_package.App"`
      - replace: `android:name="cc.binmt.signature.PmsHookApplication"`
  - file: `${app_dir}/smali/cc/binmt/signature/PmsHookApplication.smali`
    * operation:
      - search: `const-string/jumbo v6, "### Signatures Data ###"`
      - replace: `const-string/jumbo v6, "xxxxxxxxxxxxxxxxxxxxxx"`
    * operation:
      - search: `Landroid/app/Application;`
      - replace: `Lcom/my_package/App;`
* other things to bear in mind
  - file: `${app_dir}/smali/com/my_package/App.smali`
    * if present, need to remove _final_ label from _Application_ class
      ```text
        .class final public Lcom/my_package/App;
        .super Landroid/app/Application;
      ```
