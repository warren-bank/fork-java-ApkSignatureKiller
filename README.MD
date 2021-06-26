This branch contains a stripped down version of the [_ApkSignatureKiller_](https://github.com/L-JINBIN/ApkSignatureKiller) utility.

It's useful when working with an APK that has already been decompiled to smali.

#### How it differs:

* doesn't use a _config.txt_ file
* accepts a single command-line parameter: `/path/to/CERT.RSA`
  - when the APK is decompressed with `unzip`
    * this file is found in the directory: `META-INF/`
  - when the APK is decoded with `apktool`
    * this file is found in the directory: `original/META-INF/`
* prints a Base64 encoded string with signatures data to _stdout_
  - there is no line separator at the end of the output
  - when there is any kind of error
    * there is no output

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
     cert_file="${app_dir}/original/META-INF/CERT.RSA"

     signatures_data=$( java -jar "${nkstool_dir}/nkstool.jar" "$cert_file" )
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
       - replace: `const-string/jumbo v6, "${signatures_data}"`
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
