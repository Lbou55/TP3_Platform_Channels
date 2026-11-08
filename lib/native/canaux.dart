
import 'package:flutter/services.dart';

class CanauxNatifs {
  static const MethodChannel _infos = MethodChannel('appareil/infos');
  static const MethodChannel _vibreur = MethodChannel('appareil/vibrer');
  static const EventChannel _lumiere = EventChannel('capteur/lumiere');

  static const MethodChannel _audioPermission =
      MethodChannel('audio/permission');

  static const EventChannel _audioNiveau = EventChannel('audio/niveau');

  static Future<Map<dynamic, dynamic>> lireInfos() async {
    final result = await _infos.invokeMethod<Map<dynamic, dynamic>>('lire');
    return result ?? {};
  }

  static Future<String> vibrer(String motif) async {
    final result = await _vibreur.invokeMethod<String>(
      'vibrer',
      {'motif': motif},
    );
    return result ?? '';
  }

  static Stream<double> ecouterLumiere() {
    return _lumiere.receiveBroadcastStream().map(
      (event) => (event as num).toDouble(),
    );
  }

  static Future<bool> demanderPermissionAudio() async {
    final result = await _audioPermission.invokeMethod<bool>('demander');
    return result ?? false;
  }

  static Stream<double> ecouterNiveauAudio() {
    return _audioNiveau.receiveBroadcastStream().map(
      (event) => (event as num).toDouble(),
    );
  }
}
