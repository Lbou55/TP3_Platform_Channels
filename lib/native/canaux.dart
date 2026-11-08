
import 'package:flutter/services.dart';

class CanauxNatifs {

  static const MethodChannel _infos =
      MethodChannel('appareil/infos');

  static const MethodChannel _vibreur =
      MethodChannel('appareil/vibrer');

  static const EventChannel _lumiere =
      EventChannel('capteur/lumiere');

  static const EventChannel _proximite =
      EventChannel('capteur/proximite');

  static const MethodChannel _audioPermission =
      MethodChannel('audio/permission');

  static const EventChannel _audio =
      EventChannel('audio/niveau');

  static const MethodChannel _cameraPermission =
      MethodChannel('camera/permission');

  static const MethodChannel _torche =
      MethodChannel('appareil/torche');

  static Future<Map<dynamic, dynamic>> lireInfos() async {
    final resultat = await _infos.invokeMethod('lire');
    return Map<dynamic, dynamic>.from(resultat);
  }

  static Future<String> vibrer(String motif) async {
    return await _vibreur.invokeMethod(
      'vibrer',
      {'motif': motif},
    );
  }

  static Stream<double> ecouterLumiere() {
    return _lumiere.receiveBroadcastStream().map(
      (event) => (event as num).toDouble(),
    );
  }

  static Stream<bool> ecouterProximite() {
    return _proximite.receiveBroadcastStream().map(
      (event) => event as bool,
    );
  }

  static Future<bool> demanderPermissionAudio() async {
    return await _audioPermission.invokeMethod<bool>('demander') ?? false;
  }

  static Stream<double> ecouterNiveauAudio() {
    return _audio.receiveBroadcastStream().map(
      (event) => (event as num).toDouble(),
    );
  }

  static Future<bool> demanderPermissionCamera() async {
    return await _cameraPermission.invokeMethod<bool>('demander') ?? false;
  }

  static Future<String> controlerTorche(bool etat) async {
    return await _torche.invokeMethod(
      'allumer',
      {'etat': etat},
    );
  }
}
