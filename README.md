# 📱 Universal Remote

<div align="center">
  <p><strong>Transforme seu celular Android em um controle remoto físico e de baixa latência para o seu PC.</strong></p>
  
  [![Android](https://img.shields.io/badge/OS-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](#)
  [![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)](#)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)](#)
  [![License](https://img.shields.io/badge/License-MIT-blue.style=for-the-badge)](#)
</div>

<br/>

<div align="center">
  <h2><a href="https://github.com/srGabrielx/RemotoControll/releases/latest">⬇️ BAIXAR ÚLTIMA VERSÃO (APK)</a></h2>
</div>

## 🚀 Sobre o Projeto

O **Universal Remote** é um aplicativo Android nativo, projetado com foco absoluto em baixa latência, arquitetura limpa e design premium. Ele divide a lógica de controle da camada de transporte, permitindo conexões via Wi-Fi (WebSocket), e preparando o terreno para WebRTC, Bluetooth e USB.

### ✨ Funcionalidades (Client-Side)

*   **Touchpad Tátil Profissional:** Superfície com suporte a gestos multi-touch (movimento, clique com 1 ou 2 dedos, rolagem suave).
*   **Teclado Nativo:** Integração com o teclado virtual do Android para envio de texto contínuo e rápido.
*   **Controles de Mídia & Atalhos:** Botões físicos na tela para Volume, Play/Pause, ESC, TAB, Win, Ctrl+Z e Enter.
*   **Configurações de Precisão:** Ajuste de sensibilidade do mouse, velocidade de rolagem, inversão de eixo e feedback tátil (haptics).
*   **Screen Mode (Ready):** Interface preparada para receber streaming de vídeo do desktop via WebRTC com suporte a zoom e pan.
*   **Descoberta Local:** Escaneamento de dispositivos na rede e histórico de conexões recentes.

## 📦 Download e Instalação

### No Celular (Android)
1. Acesse a aba [Releases](../../releases).
2. Baixe o arquivo `app-release.apk`.
3. Abra no seu celular Android e confirme a instalação (pode ser necessário autorizar a instalação de fontes desconhecidas).

### No Computador (Host / Receiver)
*(O Agente Desktop está em desenvolvimento. A especificação do protocolo já está disponível no arquivo `REMOTE_PROTOCOL.md` caso queira criar seu próprio servidor WebSocket).*

## 🛠️ Stack Tecnológica

*   **Linguagem:** Kotlin
*   **UI:** Jetpack Compose (Material Design 3)
*   **Arquitetura:** MVVM, Coroutines, StateFlow
*   **Rede / Transporte:** OkHttp (WebSocket) + Moshi (JSON)

## 🏗️ Como Compilar o Projeto (Desenvolvedores)

1. Clone o repositório:
   ```bash
   git clone https://github.com/srGabrielx/RemotoControll.git
   ```
2. Abra o projeto no **Android Studio**.
3. Aguarde o Gradle sincronizar as dependências.
4. Conecte seu dispositivo Android via cabo USB (com Depuração USB ativa).
5. Clique em **Run** (`Shift + F10`).

## 📄 Protocolo de Comunicação

O projeto foi construído para ser agnóstico de transporte. A comunicação atual usa WebSockets transmitindo JSON.
Leia o [REMOTE_PROTOCOL.md](REMOTE_PROTOCOL.md) para entender a estrutura de payload (Eventos de Mouse, Teclado e Handshake).

## 📝 Licença

Este projeto está sob a licença MIT. Veja o arquivo [LICENSE](LICENSE) para mais detalhes.
